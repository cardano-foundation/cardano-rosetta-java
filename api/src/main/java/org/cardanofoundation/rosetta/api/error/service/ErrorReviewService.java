package org.cardanofoundation.rosetta.api.error.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.error.model.domain.entity.ErrorReview;
import org.cardanofoundation.rosetta.api.error.model.repository.ErrorReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorReviewService {

    private final ErrorReviewRepository errorReviewRepository;

    @Transactional(readOnly = true)
    public Optional<ErrorReview> findById(Integer id) {
        return errorReviewRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ErrorReview> findByIds(Iterable<Integer> ids) {
        return errorReviewRepository.findAllById(ids);
    }

    @Transactional
    public ErrorReview upsert(ErrorReview errorReview) {
        return errorReviewRepository.save(errorReview);
    }

}
