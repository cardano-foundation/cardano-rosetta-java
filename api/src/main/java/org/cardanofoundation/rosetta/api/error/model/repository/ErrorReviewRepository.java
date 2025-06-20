package org.cardanofoundation.rosetta.api.error.model.repository;

import org.cardanofoundation.rosetta.api.error.model.domain.entity.ErrorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorReviewRepository extends JpaRepository<ErrorReview, Integer> {

}
