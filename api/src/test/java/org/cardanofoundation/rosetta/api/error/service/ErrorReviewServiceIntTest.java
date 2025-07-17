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
class ErrorReviewServiceIntTest extends IntegrationTest {

    @Autowired
    private ErrorReviewService errorReviewService;

    @Autowired
    private ErrorReviewRepository errorReviewRepository;

    @Autowired
    private ErrorRepository errorRepository;

    @Nested
    @DisplayName("Database Operations")
    class DatabaseOperationsTest {

        @Test
        @DisplayName("should save and retrieve error review entity")
        void shouldSaveAndRetrieveErrorReviewEntity() {
            // given
            ErrorReviewEntity entity = ErrorReviewEntity.builder()
                .id(1)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Test comment")
                .checkedBy("test_reviewer")
                .lastUpdated(LocalDateTime.now())
                .build();

            // when
            ErrorReviewEntity savedEntity = errorReviewService.upsert(entity);
            Optional<ErrorReviewEntity> retrievedEntity = errorReviewService.findById(1);

            // then
            assertNotNull(savedEntity);
            assertTrue(retrievedEntity.isPresent());
            assertEquals(entity.getId(), retrievedEntity.get().getId());
            assertEquals(entity.getStatus(), retrievedEntity.get().getStatus());
            assertEquals(entity.getComment(), retrievedEntity.get().getComment());
            assertEquals(entity.getCheckedBy(), retrievedEntity.get().getCheckedBy());
        }

        @Test
        @DisplayName("should update existing error review entity")
        void shouldUpdateExistingErrorReviewEntity() {
            // given
            ErrorReviewEntity originalEntity = ErrorReviewEntity.builder()
                .id(2)
                .status(ReviewStatus.UNREVIEWED)
                .comment("Original comment")
                .checkedBy("original_reviewer")
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();
            errorReviewService.upsert(originalEntity);

            // when
            ErrorReviewEntity updatedEntity = ErrorReviewEntity.builder()
                .id(2)
                .status(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US)
                .comment("Updated comment")
                .checkedBy("new_reviewer")
                .lastUpdated(LocalDateTime.now())
                .build();
            errorReviewService.upsert(updatedEntity);

            Optional<ErrorReviewEntity> retrievedEntity = errorReviewService.findById(2);

            // then
            assertTrue(retrievedEntity.isPresent());
            assertEquals(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US, retrievedEntity.get().getStatus());
            assertEquals("Updated comment", retrievedEntity.get().getComment());
            assertEquals("new_reviewer", retrievedEntity.get().getCheckedBy());
        }

        @Test
        @DisplayName("should find entities by multiple IDs")
        void shouldFindEntitiesByMultipleIds() {
            // given
            ErrorReviewEntity entity1 = ErrorReviewEntity.builder()
                .id(3)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Comment 1")
                .checkedBy("reviewer1")
                .lastUpdated(LocalDateTime.now())
                .build();
            ErrorReviewEntity entity2 = ErrorReviewEntity.builder()
                .id(4)
                .status(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US)
                .comment("Comment 2")
                .checkedBy("reviewer2")
                .lastUpdated(LocalDateTime.now())
                .build();
            errorReviewService.upsert(entity1);
            errorReviewService.upsert(entity2);

            // when
            List<ErrorReviewEntity> entities = errorReviewService.findByIds(List.of(3, 4));

            // then
            assertEquals(2, entities.size());
            assertTrue(entities.stream().anyMatch(e -> e.getId().equals(3)));
            assertTrue(entities.stream().anyMatch(e -> e.getId().equals(4)));
        }
    }

    @Nested
    @DisplayName("Complex Query Operations")
    class ComplexQueryOperationsTest {

        @Test
        @DisplayName("should find all block parsing errors with left join")
        void shouldFindAllBlockParsingErrorsWithLeftJoin() {
            // given - create test data
            ErrorEntity errorEntity = new ErrorEntity(
                5,
                123456L,
                "PARSE_ERROR",
                "Failed to parse block",
                "Detailed error information",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            ErrorReviewEntity reviewEntity = ErrorReviewEntity.builder()
                .id(5)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("This affects us")
                .checkedBy("reviewer")
                .lastUpdated(LocalDateTime.now())
                .build();
            errorReviewService.upsert(reviewEntity);

            // when
            List<BlockParsingErrorReviewDTO> results = errorReviewService.findTop1000();

            // then
            assertFalse(results.isEmpty());
            Optional<BlockParsingErrorReviewDTO> result = results.stream()
                .filter(r -> r.id().equals(5))
                .findFirst();
            
            assertTrue(result.isPresent());
            assertEquals(5, result.get().id());
            assertEquals(123456L, result.get().block());
            assertEquals("PARSE_ERROR", result.get().errorCode());
            assertEquals("Failed to parse block", result.get().reason());
            assertEquals("Detailed error information", result.get().details());
            assertEquals(ReviewStatus.REVIEWED_AFFECTS_US, result.get().status());
            assertEquals("This affects us", result.get().comment());
            assertEquals("reviewer", result.get().checkedBy());
        }

        @Test
        @DisplayName("should find errors by review status")
        void shouldFindErrorsByReviewStatus() {
            // given
            ErrorEntity errorEntity1 = new ErrorEntity(
                6,
                123457L,
                "PARSE_ERROR",
                "Error 1",
                "Details 1",
                LocalDateTime.now()
            );
            ErrorEntity errorEntity2 = new ErrorEntity(
                7,
                123458L,
                "PARSE_ERROR",
                "Error 2",
                "Details 2",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity1);
            errorRepository.save(errorEntity2);

            ErrorReviewEntity reviewEntity1 = ErrorReviewEntity.builder()
                .id(6)
                .status(ReviewStatus.REVIEWED_AFFECTS_US)
                .comment("Affects us")
                .checkedBy("reviewer")
                .lastUpdated(LocalDateTime.now())
                .build();
            ErrorReviewEntity reviewEntity2 = ErrorReviewEntity.builder()
                .id(7)
                .status(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US)
                .comment("Does not affect us")
                .checkedBy("reviewer")
                .lastUpdated(LocalDateTime.now())
                .build();
            errorReviewService.upsert(reviewEntity1);
            errorReviewService.upsert(reviewEntity2);

            // when
            List<BlockParsingErrorReviewDTO> affectsUsResults = 
                errorReviewService.findTop1000ByReviewStatus(ReviewStatus.REVIEWED_AFFECTS_US);
            List<BlockParsingErrorReviewDTO> doesNotAffectUsResults = 
                errorReviewService.findTop1000ByReviewStatus(ReviewStatus.REVIEWED_DOES_NOT_AFFECT_US);

            // then
            assertTrue(affectsUsResults.stream().anyMatch(r -> r.id().equals(6)));
            assertTrue(doesNotAffectUsResults.stream().anyMatch(r -> r.id().equals(7)));
            assertFalse(affectsUsResults.stream().anyMatch(r -> r.id().equals(7)));
            assertFalse(doesNotAffectUsResults.stream().anyMatch(r -> r.id().equals(6)));
        }

        @Test
        @DisplayName("should find errors by block number")
        void shouldFindErrorsByBlockNumber() {
            // given
            long blockNumber = 999999L;
            ErrorEntity errorEntity = new ErrorEntity(
                8,
                blockNumber,
                "PARSE_ERROR",
                "Block specific error",
                "Details for block",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            ErrorReviewEntity reviewEntity = ErrorReviewEntity.builder()
                .id(8)
                .status(ReviewStatus.UNREVIEWED)
                .lastUpdated(LocalDateTime.now())
                .build();
            errorReviewService.upsert(reviewEntity);

            // when
            List<BlockParsingErrorReviewDTO> results = 
                errorReviewService.findTop1000ByBlockNumber(blockNumber);

            // then
            assertFalse(results.isEmpty());
            Optional<BlockParsingErrorReviewDTO> result = results.stream()
                .filter(r -> r.id().equals(8))
                .findFirst();
            
            assertTrue(result.isPresent());
            assertEquals(blockNumber, result.get().block());
            assertEquals("Block specific error", result.get().reason());
            assertEquals(ReviewStatus.UNREVIEWED, result.get().status());
        }

        @Test
        @DisplayName("should handle unreviewed errors with null review status")
        void shouldHandleUnreviewedErrorsWithNullReviewStatus() {
            // given - create error without review
            ErrorEntity errorEntity = new ErrorEntity(
                9,
                777777L,
                "PARSE_ERROR",
                "Unreviewed error",
                "No review yet",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            // when
            List<BlockParsingErrorReviewDTO> results = errorReviewService.findTop1000();

            // then
            Optional<BlockParsingErrorReviewDTO> result = results.stream()
                .filter(r -> r.id().equals(9))
                .findFirst();
            
            assertTrue(result.isPresent());
            assertEquals(ReviewStatus.UNREVIEWED, result.get().status());
            assertNull(result.get().comment());
            assertNull(result.get().checkedBy());
        }

        @Test
        @DisplayName("should include proper note with block explorer link")
        void shouldIncludeProperNoteWithBlockExplorerLink() {
            // given
            long blockNumber = 555555L;
            ErrorEntity errorEntity = new ErrorEntity(
                10,
                blockNumber,
                "PARSE_ERROR",
                "Test error",
                "Test details",
                LocalDateTime.now()
            );
            errorRepository.save(errorEntity);

            // when
            List<BlockParsingErrorReviewDTO> results = errorReviewService.findTop1000();

            // then
            Optional<BlockParsingErrorReviewDTO> result = results.stream()
                .filter(r -> r.id().equals(10))
                .findFirst();
            
            assertTrue(result.isPresent());
            String expectedNote = "Please review all transactions within a block, https://explorer.cardano.org/block/" + blockNumber;
            assertEquals(expectedNote, result.get().note());
        }
    }
}