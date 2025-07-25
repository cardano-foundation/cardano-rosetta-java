package org.cardanofoundation.rosetta.api.error.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorEntity;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorReviewEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNullElse;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockParsingErrorReviewService {

    public static final String ANONYMOUS_USER = "anonymous";

    private final ErrorService errorService;
    private final ErrorReviewService errorReviewService;
    private final Clock clock;

    @Transactional
    public List<BlockParsingErrorReviewDTO> findTop1000(@Nullable ReviewStatus status) {
        if (status != null) {
            return errorReviewService.findTop1000ByReviewStatus(status);
        }

        return errorReviewService.findTop1000();
    }

    @Transactional(readOnly = true)
    public List<BlockParsingErrorReviewDTO> findTop1000ByBlockNumber(long blockNumber) {
        return errorReviewService.findTop1000ByBlockNumber(blockNumber);
    }

    @Transactional
    public Optional<ErrorReviewEntity> upsert(Integer errorId,
                                             ReviewStatus status,
                                             @Nullable String comment,
                                             @Nullable String checkedBy) {
        Optional<ErrorEntity> errorM = errorService.findById(errorId);

        if (errorM.isEmpty()) {
            return Optional.empty();
        }

        ErrorReviewEntity errorReviewEntity = errorReviewService.findById(errorId)
                .orElseGet(() -> ErrorReviewEntity.builder()
                        .id(errorId)
                        .build());

        // mandatory param
        errorReviewEntity.setStatus(status);
        errorReviewEntity.setCheckedBy(requireNonNullElse(checkedBy, ANONYMOUS_USER));

        if (comment != null) {
            errorReviewEntity.setComment(comment);
        }

        errorReviewEntity.setLastUpdated(LocalDateTime.now(clock));

        return Optional.of(errorReviewService.upsert(errorReviewEntity));
    }

}
