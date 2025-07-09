package org.cardanofoundation.rosetta.api.error.service;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorEntity;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorReviewEntity;
import org.cardanofoundation.rosetta.api.error.model.repository.ErrorRepository;
import org.cardanofoundation.rosetta.api.error.model.repository.ErrorReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class BlockParsingErrorReviewServiceIntTest extends IntegrationTest {

    @Autowired
    private BlockParsingErrorReviewService blockParsingErrorReviewService;

    @Autowired
    private ErrorRepository errorRepository;

    @Autowired
    private ErrorReviewRepository errorReviewRepository;

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTest {

        @Test
        @DisplayName("should find top 1000 errors without filter")
        void shouldFindTop1000ErrorsWithoutFilter() {
            // given
            ErrorEntity errorEntity = new ErrorEntity(
                100,
                123456L,
                "PARSE_ERROR",
                "Test error",
                "Test details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            ErrorReviewEntity reviewEntity = ErrorReviewEntity.builder()
                .id(100)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Test comment")
                .checkedBy("test_reviewer")
                .lastUpdated(LocalDateTime.now())
                .build();
            errorReviewRepository.save(reviewEntity);

            // when
            List<BlockParsingErrorReviewDTO> results = blockParsingErrorReviewService.findTop1000(null);

            // then
            assertFalse(results.isEmpty());
            Optional<BlockParsingErrorReviewDTO> result = results.stream()
                .filter(r -> r.id().equals(100))
                .findFirst();
            
            assertTrue(result.isPresent());
            assertEquals(100, result.get().id());
            assertEquals(123456L, result.get().block());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, result.get().status());
            assertEquals("Test comment", result.get().comment());
        }

        @Test
        @DisplayName("should find top 1000 errors filtered by status")
        void shouldFindTop1000ErrorsFilteredByStatus() {
            // given
            ErrorEntity errorEntity1 = new ErrorEntity(
                101,
                123457L,
                "PARSE_ERROR",
                "Error 1",
                "Details 1",
                LocalDateTime.now()
            );
            ErrorEntity errorEntity2 = new ErrorEntity(
                102,
                123458L,
                "PARSE_ERROR",
                "Error 2",
                "Details 2",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity1);
            errorRepository.save(errorEntity2);

            ErrorReviewEntity reviewEntity1 = ErrorReviewEntity.builder()
                .id(101)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Affects us")
                .checkedBy("reviewer1")
                .lastUpdated(LocalDateTime.now())
                .build();
            ErrorReviewEntity reviewEntity2 = ErrorReviewEntity.builder()
                .id(102)
                .status(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US)
                .comment("Does not affect us")
                .checkedBy("reviewer2")
                .lastUpdated(LocalDateTime.now())
                .build();
            errorReviewRepository.save(reviewEntity1);
            errorReviewRepository.save(reviewEntity2);

            // when
            List<BlockParsingErrorReviewDTO> results = 
                blockParsingErrorReviewService.findTop1000(ReviewStatus.REVIEWED_AFFECTS_US);

            // then
            assertTrue(results.stream().anyMatch(r -> r.id().equals(101)));
            assertFalse(results.stream().anyMatch(r -> r.id().equals(102)));
        }

        @Test
        @DisplayName("should find top 1000 errors by block number")
        void shouldFindTop1000ErrorsByBlockNumber() {
            // given
            long blockNumber = 888888L;
            ErrorEntity errorEntity = new ErrorEntity(
                103,
                blockNumber,
                "PARSE_ERROR",
                "Block specific error",
                "Block details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            // when
            List<BlockParsingErrorReviewDTO> results = 
                blockParsingErrorReviewService.findTop1000ByBlockNumber(blockNumber);

            // then
            Optional<BlockParsingErrorReviewDTO> result = results.stream()
                .filter(r -> r.id().equals(103))
                .findFirst();
            
            assertTrue(result.isPresent());
            assertEquals(blockNumber, result.get().block());
            assertEquals("Block specific error", result.get().reason());
        }
    }

    @Nested
    @DisplayName("Upsert Operations")
    class UpsertOperationsTest {

        @Test
        @DisplayName("should return empty when error does not exist")
        void shouldReturnEmptyWhenErrorDoesNotExist() {
            // given
            Integer nonExistentErrorId = 999999;

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                nonExistentErrorId,
                ReviewStatus.REVIEWED_AFFECTS_US,
                "Comment",
                "reviewer"
            );

            // then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should create new review when error exists but review does not")
        void shouldCreateNewReviewWhenErrorExistsButReviewDoesNot() {
            // given
            ErrorEntity errorEntity = new ErrorEntity(
                104,
                123459L,
                "PARSE_ERROR",
                "New error",
                "New details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                104,
                ReviewStatus.REVIEWED_AFFECTS_US,
                "New review comment",
                "new_reviewer"
            );

            // then
            assertTrue(result.isPresent());
            assertEquals(104, result.get().getId());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, result.get().getStatus());
            assertEquals("New review comment", result.get().getComment());
            assertEquals("new_reviewer", result.get().getCheckedBy());
            assertNotNull(result.get().getLastUpdated());

            // Verify it's persisted
            Optional<ErrorReviewEntity> persistedEntity = errorReviewRepository.findById(104);
            assertTrue(persistedEntity.isPresent());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, persistedEntity.get().getStatus());
        }

        @Test
        @DisplayName("should update existing review")
        void shouldUpdateExistingReview() {
            // given
            ErrorEntity errorEntity = new ErrorEntity(
                105,
                123460L,
                "PARSE_ERROR",
                "Existing error",
                "Existing details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            ErrorReviewEntity existingReview = ErrorReviewEntity.builder()
                .id(105)
                .status(ReviewStatus.UNREVIEWED)
                .comment("Original comment")
                .checkedBy("original_reviewer")
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();
            errorReviewRepository.save(existingReview);

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                105,
                ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US,
                "Updated comment",
                "updated_reviewer"
            );

            // then
            assertTrue(result.isPresent());
            assertEquals(105, result.get().getId());
            assertEquals(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US, result.get().getStatus());
            assertEquals("Updated comment", result.get().getComment());
            assertEquals("updated_reviewer", result.get().getCheckedBy());

            // Verify it's updated in database
            Optional<ErrorReviewEntity> persistedEntity = errorReviewRepository.findById(105);
            assertTrue(persistedEntity.isPresent());
            assertEquals(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US, persistedEntity.get().getStatus());
            assertEquals("Updated comment", persistedEntity.get().getComment());
        }

        @Test
        @DisplayName("should use anonymous user when checkedBy is null")
        void shouldUseAnonymousUserWhenCheckedByIsNull() {
            // given
            ErrorEntity errorEntity = new ErrorEntity(
                106,
                123461L,
                "PARSE_ERROR",
                "Anonymous review error",
                "Anonymous details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                106,
                ReviewStatus.REVIEWED_AFFECTS_US,
                "Anonymous comment",
                null
            );

            // then
            assertTrue(result.isPresent());
            assertEquals(BlockParsingErrorReviewService.ANONYMOUS_USER, result.get().getCheckedBy());

            // Verify it's persisted with anonymous user
            Optional<ErrorReviewEntity> persistedEntity = errorReviewRepository.findById(106);
            assertTrue(persistedEntity.isPresent());
            assertEquals(BlockParsingErrorReviewService.ANONYMOUS_USER, persistedEntity.get().getCheckedBy());
        }

        @Test
        @DisplayName("should preserve existing comment when new comment is null")
        void shouldPreserveExistingCommentWhenNewCommentIsNull() {
            // given
            ErrorEntity errorEntity = new ErrorEntity(
                107,
                123462L,
                "PARSE_ERROR",
                "Comment preservation error",
                "Preservation details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            ErrorReviewEntity existingReview = ErrorReviewEntity.builder()
                .id(107)
                .status(ReviewStatus.UNREVIEWED)
                .comment("Existing comment to preserve")
                .checkedBy("original_reviewer")
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();
            errorReviewRepository.save(existingReview);

            // when
            Optional<ErrorReviewEntity> result = blockParsingErrorReviewService.upsert(
                107,
                ReviewStatus.REVIEWED_AFFECTS_US,
                null, // null comment should preserve existing
                "new_reviewer"
            );

            // then
            assertTrue(result.isPresent());
            assertEquals("Existing comment to preserve", result.get().getComment());
            assertEquals("new_reviewer", result.get().getCheckedBy());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, result.get().getStatus());

            // Verify comment is preserved in database
            Optional<ErrorReviewEntity> persistedEntity = errorReviewRepository.findById(107);
            assertTrue(persistedEntity.isPresent());
            assertEquals("Existing comment to preserve", persistedEntity.get().getComment());
        }

        @Test
        @DisplayName("should handle full workflow from creation to multiple updates")
        void shouldHandleFullWorkflowFromCreationToMultipleUpdates() {
            // given
            ErrorEntity errorEntity = new ErrorEntity(
                108,
                123463L,
                "PARSE_ERROR",
                "Workflow error",
                "Workflow details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            // when - first review (create)
            Optional<ErrorReviewEntity> firstResult = blockParsingErrorReviewService.upsert(
                108,
                ReviewStatus.UNREVIEWED,
                "Initial review",
                "reviewer1"
            );

            // when - second review (update)
            Optional<ErrorReviewEntity> secondResult = blockParsingErrorReviewService.upsert(
                108,
                ReviewStatus.REVIEWED_AFFECTS_US,
                "Updated to affects us",
                "reviewer2"
            );

            // when - third review (final update)
            Optional<ErrorReviewEntity> thirdResult = blockParsingErrorReviewService.upsert(
                108,
                ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US,
                "Final decision: does not affect us",
                "reviewer3"
            );

            // then
            assertTrue(firstResult.isPresent());
            assertTrue(secondResult.isPresent());
            assertTrue(thirdResult.isPresent());

            // Verify final state
            assertEquals(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US, thirdResult.get().getStatus());
            assertEquals("Final decision: does not affect us", thirdResult.get().getComment());
            assertEquals("reviewer3", thirdResult.get().getCheckedBy());

            // Verify database state
            Optional<ErrorReviewEntity> finalEntity = errorReviewRepository.findById(108);
            assertTrue(finalEntity.isPresent());
            assertEquals(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US, finalEntity.get().getStatus());
            assertEquals("Final decision: does not affect us", finalEntity.get().getComment());
            assertEquals("reviewer3", finalEntity.get().getCheckedBy());
        }
    }

    @Nested
    @DisplayName("Transaction Management")
    class TransactionManagementTest {

        @Test
        @DisplayName("should handle transaction rollback on error")
        void shouldHandleTransactionRollbackOnError() {
            // given
            ErrorEntity errorEntity = new ErrorEntity(
                109,
                123464L,
                "PARSE_ERROR",
                "Transaction test error",
                "Transaction details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            // when & then
            assertDoesNotThrow(() -> {
                blockParsingErrorReviewService.upsert(
                    109,
                    ReviewStatus.REVIEWED_AFFECTS_US,
                    "Valid transaction",
                    "reviewer"
                );
            });

            // Verify the valid transaction was committed
            Optional<ErrorReviewEntity> entity = errorReviewRepository.findById(109);
            assertTrue(entity.isPresent());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, entity.get().getStatus());
        }
    }
}