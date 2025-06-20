package org.cardanofoundation.rosetta.api.error.service;

import org.cardanofoundation.rosetta.api.error.model.entity.ErrorEntity;
import org.cardanofoundation.rosetta.api.error.model.repository.ErrorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorServiceTest {

    @Mock
    private ErrorRepository errorRepository;

    @InjectMocks
    private ErrorService errorService;

    @Nested
    @DisplayName("findById")
    class FindByIdTest {

        @Test
        @DisplayName("should return error entity when found")
        void shouldReturnErrorEntityWhenFound() {
            // given
            Integer errorId = 1;
            ErrorEntity errorEntity = new ErrorEntity(
                errorId,
                123456L,
                "PARSE_ERROR",
                "Failed to parse block",
                "Detailed error information",
                LocalDateTime.now()
            );
            when(errorRepository.findById(errorId)).thenReturn(Optional.of(errorEntity));

            // when
            Optional<ErrorEntity> result = errorService.findById(errorId);

            // then
            assertTrue(result.isPresent());
            assertEquals(errorId, result.get().getId());
            assertEquals(123456L, result.get().getBlock());
            assertEquals("PARSE_ERROR", result.get().getErrorCode());
            assertEquals("Failed to parse block", result.get().getReason());
            assertEquals("Detailed error information", result.get().getDetails());
        }

        @Test
        @DisplayName("should return empty optional when not found")
        void shouldReturnEmptyOptionalWhenNotFound() {
            // given
            Integer errorId = 999;
            when(errorRepository.findById(errorId)).thenReturn(Optional.empty());

            // when
            Optional<ErrorEntity> result = errorService.findById(errorId);

            // then
            assertTrue(result.isEmpty());
        }
    }
}