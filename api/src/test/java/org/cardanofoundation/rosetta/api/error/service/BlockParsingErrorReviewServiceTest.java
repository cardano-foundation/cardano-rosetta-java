package org.cardanofoundation.rosetta.api.error.service;

import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorEntity;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorReviewEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockParsingErrorReviewServiceTest {

    @Mock
    private ErrorService errorService;

    @Mock
    private ErrorReviewService errorReviewService;

    @Mock
    private Clock clock;

    @InjectMocks
    private BlockParsingErrorReviewService blockParsingErrorReviewService;

    private final LocalDateTime fixedTime = LocalDateTime.of(2025, 6, 20, 12, 30, 0);

    @Nested
    @DisplayName("findTop1000")
    class FindTop1000Test {

        @Test
        @DisplayName("should return top 1000 errors filtered by status when status is provided")
        void shouldReturnTop1000ErrorsFilteredByStatusWhenStatusIsProvided() {
            // given
            ReviewStatus status = ReviewStatus.UNREVIEWED;
            List<BlockParsingErrorReviewDTO> expectedResults = List.of(
                new BlockParsingErrorReviewDTO(1, 123456L, "PARSE_ERROR", "Failed to parse", "Details", 
                    status, null, null, "Note", fixedTime)
            );
            when(errorReviewService.findTop1000ByReviewStatus(status)).thenReturn(expectedResults);

            // when
            List<BlockParsingErrorReviewDTO> result = blockParsingErrorReviewService.findTop1000(status);

            // then
            assertEquals(1, result.size());
            assertEquals(status, result.get(0).status());
            verify(errorReviewService).findTop1000ByReviewStatus(status);
        }

        @Test
        @DisplayName("should return top 1000 errors without filter when status is null")
        void shouldReturnTop1000ErrorsWithoutFilterWhenStatusIsNull() {
            // given
            List<BlockParsingErrorReviewDTO> expectedResults = List.of(
                new BlockParsingErrorReviewDTO(1, 123456L, "PARSE_ERROR", "Failed to parse", "Details", 
                    ReviewStatus.UNREVIEWED, null, null, "Note", fixedTime)
            );
            when(errorReviewService.findTop1000()).thenReturn(expectedResults);

            // when
            List<BlockParsingErrorReviewDTO> result = blockParsingErrorReviewService.findTop1000(null);

            // then
            assertEquals(1, result.size());
            verify(errorReviewService).findTop1000();
        }
    }

    @Nested
    @DisplayName("findTop1000ByBlockNumber")
    class FindTop1000ByBlockNumberTest {

        @Test
        @DisplayName("should return top 1000 errors for specific block number")
        void shouldReturnTop1000ErrorsForSpecificBlockNumber() {
            // given
            long blockNumber = 123456L;
            List<BlockParsingErrorReviewDTO> expectedResults = List.of(
                new BlockParsingErrorReviewDTO(1, blockNumber, "PARSE_ERROR", "Failed to parse", "Details", 
                    ReviewStatus.UNREVIEWED, null, null, "Note", fixedTime)
            );
            when(errorReviewService.findTop1000ByBlockNumber(blockNumber)).thenReturn(expectedResults);

            // when
            List<BlockParsingErrorReviewDTO> result = blockParsingErrorReviewService.findTop1000ByBlockNumber(blockNumber);

            // then
            assertEquals(1, result.size());
            assertEquals(blockNumber, result.get(0).block());
            verify(errorReviewService).findTop1000ByBlockNumber(blockNumber);
        }
    }

    @Nested
    @DisplayName("upsert")
    class UpsertTest {

        @Test
        @DisplayName("should return empty when error not found")
        void shouldReturnEmptyWhenErrorNotFound() {
            // given
            Integer errorId = 999;
            when(errorService.findById(errorId)).thenReturn(Optional.empty());

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                errorId, ReviewStatus.REVIEWED_AFFECTS_US, "Test comment", "reviewer1"
            );

            // then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should create new review when error exists but review does not")
        void shouldCreateNewReviewWhenErrorExistsButReviewDoesNot() {
            // given
            Integer errorId = 1;
            ErrorEntity errorEntity = new ErrorEntity(errorId, 123456L, "PARSE_ERROR", "Failed to parse", "Details", fixedTime);
            when(errorService.findById(errorId)).thenReturn(Optional.of(errorEntity));
            when(errorReviewService.findById(errorId)).thenReturn(Optional.empty());
            when(clock.instant()).thenReturn(ZonedDateTime.of(fixedTime, ZoneId.systemDefault()).toInstant());
            when(clock.getZone()).thenReturn(ZoneId.systemDefault());

            ErrorReviewEntity newEntity = ErrorReviewEntity.builder()
                .id(errorId)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Test comment")
                .checkedBy("reviewer1")
                .lastUpdated(fixedTime)
                .build();
            when(errorReviewService.upsert(any(ErrorReviewEntity.class))).thenReturn(newEntity);

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                errorId, ReviewStatus.REVIEWED_AFFECTS_US, "Test comment", "reviewer1"
            );

            // then
            assertTrue(result.isPresent());
            assertEquals(errorId, result.get().getId());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, result.get().getStatus());
            assertEquals("Test comment", result.get().getComment());
            assertEquals("reviewer1", result.get().getCheckedBy());
            verify(errorReviewService).upsert(any(ErrorReviewEntity.class));
        }

        @Test
        @DisplayName("should update existing review when it exists")
        void shouldUpdateExistingReviewWhenItExists() {
            // given
            Integer errorId = 1;
            ErrorEntity errorEntity = new ErrorEntity(errorId, 123456L, "PARSE_ERROR", "Failed to parse", "Details", fixedTime);
            ErrorReviewEntity existingEntity = ErrorReviewEntity.builder()
                .id(errorId)
                .status(ReviewStatus.UNREVIEWED)
                .comment("Old comment")
                .checkedBy("old_reviewer")
                .lastUpdated(fixedTime.minusDays(1))
                .build();

            when(errorService.findById(errorId)).thenReturn(Optional.of(errorEntity));
            when(errorReviewService.findById(errorId)).thenReturn(Optional.of(existingEntity));
            when(clock.instant()).thenReturn(ZonedDateTime.of(fixedTime, ZoneId.systemDefault()).toInstant());
            when(clock.getZone()).thenReturn(ZoneId.systemDefault());

            ErrorReviewEntity updatedEntity = ErrorReviewEntity.builder()
                .id(errorId)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Updated comment")
                .checkedBy("new_reviewer")
                .lastUpdated(fixedTime)
                .build();
            when(errorReviewService.upsert(any(ErrorReviewEntity.class))).thenReturn(updatedEntity);

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                errorId, ReviewStatus.REVIEWED_AFFECTS_US, "Updated comment", "new_reviewer"
            );

            // then
            assertTrue(result.isPresent());
            assertEquals(errorId, result.get().getId());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, result.get().getStatus());
            assertEquals("Updated comment", result.get().getComment());
            assertEquals("new_reviewer", result.get().getCheckedBy());
            verify(errorReviewService).upsert(any(ErrorReviewEntity.class));
        }

        @Test
        @DisplayName("should use anonymous user when checkedBy is null")
        void shouldUseAnonymousUserWhenCheckedByIsNull() {
            // given
            Integer errorId = 1;
            ErrorEntity errorEntity = new ErrorEntity(errorId, 123456L, "PARSE_ERROR", "Failed to parse", "Details", fixedTime);
            when(errorService.findById(errorId)).thenReturn(Optional.of(errorEntity));
            when(errorReviewService.findById(errorId)).thenReturn(Optional.empty());
            when(clock.instant()).thenReturn(ZonedDateTime.of(fixedTime, ZoneId.systemDefault()).toInstant());
            when(clock.getZone()).thenReturn(ZoneId.systemDefault());

            ErrorReviewEntity newEntity = ErrorReviewEntity.builder()
                .id(errorId)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Test comment")
                .checkedBy(BlockParsingErrorReviewService.ANONYMOUS_USER)
                .lastUpdated(fixedTime)
                .build();
            when(errorReviewService.upsert(any(ErrorReviewEntity.class))).thenReturn(newEntity);

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                errorId, ReviewStatus.REVIEWED_AFFECTS_US, "Test comment", null
            );

            // then
            assertTrue(result.isPresent());
            assertEquals(BlockParsingErrorReviewService.ANONYMOUS_USER, result.get().getCheckedBy());
        }

        @Test
        @DisplayName("should preserve existing comment when new comment is null")
        void shouldPreserveExistingCommentWhenNewCommentIsNull() {
            // given
            Integer errorId = 1;
            ErrorEntity errorEntity = new ErrorEntity(errorId, 123456L, "PARSE_ERROR", "Failed to parse", "Details", fixedTime);
            ErrorReviewEntity existingEntity = ErrorReviewEntity.builder()
                .id(errorId)
                .status(ReviewStatus.UNREVIEWED)
                .comment("Existing comment")
                .checkedBy("old_reviewer")
                .lastUpdated(fixedTime.minusDays(1))
                .build();

            when(errorService.findById(errorId)).thenReturn(Optional.of(errorEntity));
            when(errorReviewService.findById(errorId)).thenReturn(Optional.of(existingEntity));
            when(clock.instant()).thenReturn(ZonedDateTime.of(fixedTime, ZoneId.systemDefault()).toInstant());
            when(clock.getZone()).thenReturn(ZoneId.systemDefault());

            ErrorReviewEntity updatedEntity = ErrorReviewEntity.builder()
                .id(errorId)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Existing comment") // Should preserve existing comment
                .checkedBy("new_reviewer")
                .lastUpdated(fixedTime)
                .build();
            when(errorReviewService.upsert(any(ErrorReviewEntity.class))).thenReturn(updatedEntity);

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                errorId, ReviewStatus.REVIEWED_AFFECTS_US, null, "new_reviewer"
            );

            // then
            assertTrue(result.isPresent());
            assertEquals("Existing comment", result.get().getComment());
        }
    }
}