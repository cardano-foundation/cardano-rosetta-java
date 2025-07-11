package org.cardanofoundation.rosetta.api.error.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorReviewEntity;
import org.cardanofoundation.rosetta.api.error.model.repository.ErrorReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorReviewService {

    public static final int MAX_RESULTS = 1000;
    private final ErrorReviewRepository errorReviewRepository;

    @Transactional(readOnly = true)
    public Optional<ErrorReviewEntity> findById(Integer id) {
        return errorReviewRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ErrorReviewEntity> findByIds(Iterable<Integer> ids) {
        return errorReviewRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public List<BlockParsingErrorReviewDTO> findTop1000() {
        PageRequest p = PageRequest.ofSize(MAX_RESULTS);

        return errorReviewRepository.findAllBlockParsingErrors(p);
    }

    @Transactional(readOnly = true)
    public List<BlockParsingErrorReviewDTO> findTop1000ByReviewStatus(ReviewStatus status) {
        PageRequest p = PageRequest.ofSize(MAX_RESULTS);

        return errorReviewRepository.findAllBlockParsingErrorsByReviewStatus(status, p);
    }

    @Transactional(readOnly = true)
    public List<BlockParsingErrorReviewDTO> findTop1000ByBlockNumber(long blockNumber) {
        PageRequest p = PageRequest.ofSize(MAX_RESULTS);

        return errorReviewRepository.findAllBlockParsingErrorsByBlockNumber(blockNumber, p);
    }

    @Transactional
    public ErrorReviewEntity upsert(ErrorReviewEntity errorReviewEntity) {
        return errorReviewRepository.save(errorReviewEntity);
    }

}
