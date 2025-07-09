package org.cardanofoundation.rosetta.api.error.service;

import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorReviewEntity;
import org.cardanofoundation.rosetta.api.error.model.repository.ErrorReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorReviewServiceTest {

    @Mock
    private ErrorReviewRepository errorReviewRepository;

    @InjectMocks
    private ErrorReviewService errorReviewService;

    @Nested
    @DisplayName("findById")
    class FindByIdTest {

        @Test
        @DisplayName("should return error review entity when found")
        void shouldReturnErrorReviewEntityWhenFound() {
            // given
            Integer errorId = 1;
            ErrorReviewEntity errorReviewEntity = ErrorReviewEntity.builder()
                .id(errorId)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("This affects our system")
                .checkedBy("reviewer1")
                .lastUpdated(LocalDateTime.now())
                .build();
            when(errorReviewRepository.findById(errorId)).thenReturn(Optional.of(errorReviewEntity));

            // when
            Optional<ErrorReviewEntity> result = errorReviewService.findById(errorId);

            // then
            assertTrue(result.isPresent());
            assertEquals(errorId, result.get().getId());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, result.get().getStatus());
            assertEquals("This affects our system", result.get().getComment());
            assertEquals("reviewer1", result.get().getCheckedBy());
        }

        @Test
        @DisplayName("should return empty optional when not found")
        void shouldReturnEmptyOptionalWhenNotFound() {
            // given
            Integer errorId = 999;
            when(errorReviewRepository.findById(errorId)).thenReturn(Optional.empty());

            // when
            Optional<ErrorReviewEntity> result = errorReviewService.findById(errorId);

            // then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByIds")
    class FindByIdsTest {

        @Test
        @DisplayName("should return list of error review entities")
        void shouldReturnListOfErrorReviewEntities() {
            // given
            List<Integer> ids = List.of(1, 2, 3);
            List<ErrorReviewEntity> entities = List.of(
                ErrorReviewEntity.builder().id(1).status(ReviewStatus.UNREVIEWED).build(),
                ErrorReviewEntity.builder().id(2).status(ReviewStatus.REVIEWED_AFFECTS_US).build(),
                ErrorReviewEntity.builder().id(3).status(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US).build()
            );
            when(errorReviewRepository.findAllById(ids)).thenReturn(entities);

            // when
            List<ErrorReviewEntity> result = errorReviewService.findByIds(ids);

            // then
            assertEquals(3, result.size());
            assertEquals(1, result.get(0).getId());
            assertEquals(2, result.get(1).getId());
            assertEquals(3, result.get(2).getId());
        }
    }

    @Nested
    @DisplayName("findTop1000")
    class FindTop1000Test {

        @Test
        @DisplayName("should return top 1000 block parsing errors")
        void shouldReturnTop1000BlockParsingErrors() {
            // given
            List<BlockParsingErrorReviewDTO> expectedResults = List.of(
                new BlockParsingErrorReviewDTO(1, 123456L, "PARSE_ERROR", "Failed to parse", "Details", 
                    ReviewStatus.UNREVIEWED, null, null, "Note", LocalDateTime.now())
            );
            when(errorReviewRepository.findAllBlockParsingErrors(any(PageRequest.class)))
                .thenReturn(expectedResults);

            // when
            List<BlockParsingErrorReviewDTO> result = errorReviewService.findTop1000();

            // then
            assertEquals(1, result.size());
            assertEquals(1, result.get(0).id());
            assertEquals(123456L, result.get(0).block());
            assertEquals("PARSE_ERROR", result.get(0).errorCode());
            verify(errorReviewRepository).findAllBlockParsingErrors(PageRequest.ofSize(1000));
        }
    }

    @Nested
    @DisplayName("findTop1000ByReviewStatus")
    class FindTop1000ByReviewStatusTest {

        @Test
        @DisplayName("should return top 1000 errors by review status")
        void shouldReturnTop1000ErrorsByReviewStatus() {
            // given
            ReviewStatus status = ReviewStatus.UNREVIEWED;
            List<BlockParsingErrorReviewDTO> expectedResults = List.of(
                new BlockParsingErrorReviewDTO(1, 123456L, "PARSE_ERROR", "Failed to parse", "Details", 
                    status, null, null, "Note", LocalDateTime.now())
            );
            when(errorReviewRepository.findAllBlockParsingErrorsByReviewStatus(any(ReviewStatus.class), any(PageRequest.class)))
                .thenReturn(expectedResults);

            // when
            List<BlockParsingErrorReviewDTO> result = errorReviewService.findTop1000ByReviewStatus(status);

            // then
            assertEquals(1, result.size());
            assertEquals(status, result.get(0).status());
            verify(errorReviewRepository).findAllBlockParsingErrorsByReviewStatus(status, PageRequest.ofSize(1000));
        }
    }

    @Nested
    @DisplayName("findTop1000ByBlockNumber")
    class FindTop1000ByBlockNumberTest {

        @Test
        @DisplayName("should return top 1000 errors by block number")
        void shouldReturnTop1000ErrorsByBlockNumber() {
            // given
            long blockNumber = 123456L;
            List<BlockParsingErrorReviewDTO> expectedResults = List.of(
                new BlockParsingErrorReviewDTO(1, blockNumber, "PARSE_ERROR", "Failed to parse", "Details", 
                    ReviewStatus.UNREVIEWED, null, null, "Note", LocalDateTime.now())
            );
            when(errorReviewRepository.findAllBlockParsingErrorsByBlockNumber(any(Long.class), any(PageRequest.class)))
                .thenReturn(expectedResults);

            // when
            List<BlockParsingErrorReviewDTO> result = errorReviewService.findTop1000ByBlockNumber(blockNumber);

            // then
            assertEquals(1, result.size());
            assertEquals(blockNumber, result.get(0).block());
            verify(errorReviewRepository).findAllBlockParsingErrorsByBlockNumber(blockNumber, PageRequest.ofSize(1000));
        }
    }

    @Nested
    @DisplayName("upsert")
    class UpsertTest {

        @Test
        @DisplayName("should save and return error review entity")
        void shouldSaveAndReturnErrorReviewEntity() {
            // given
            ErrorReviewEntity entity = ErrorReviewEntity.builder()
                .id(1)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Test comment")
                .checkedBy("reviewer1")
                .lastUpdated(LocalDateTime.now())
                .build();
            when(errorReviewRepository.save(entity)).thenReturn(entity);

            // when
            ErrorReviewEntity result = errorReviewService.upsert(entity);

            // then
            assertEquals(entity, result);
            verify(errorReviewRepository).save(entity);
        }
    }
}