package org.cardanofoundation.rosetta.api.error.model.repository;

import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorReviewEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorReviewRepository extends JpaRepository<ErrorReviewEntity, Integer> {

    @Query("""
        SELECT new org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO(
            e.id,
            e.block,
            e.errorCode,
            e.reason,
            e.details,
            CASE
                WHEN r.status IS NULL THEN 'UNREVIEWED'
                ELSE r.status
            END,
            r.comment,
            r.checkedBy,
            'Please review all transactions within a block, https://explorer.cardano.org/block/' || e.block,
            COALESCE(r.lastUpdated, e.updateDateTime)
        )
        FROM ErrorEntity e
        LEFT JOIN ErrorReviewEntity r ON e.id = r.id
        ORDER BY COALESCE(r.lastUpdated, e.updateDateTime) DESC
    """)
    List<BlockParsingErrorReviewDTO> findAllBlockParsingErrors(Pageable p);

    @Query("""
        SELECT new org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO(
            e.id,
            e.block,
            e.errorCode,
            e.reason,
            e.details,
            CASE
                WHEN r.status IS NULL THEN 'UNREVIEWED'
                ELSE r.status
            END,
            r.comment,
            r.checkedBy,
            'Please review all transactions within a block, https://explorer.cardano.org/block/' || e.block,
            COALESCE(r.lastUpdated, e.updateDateTime)
        )
        FROM ErrorEntity e
        LEFT JOIN ErrorReviewEntity r ON e.id = r.id
        WHERE (:status IS NULL OR (
            :status = 'UNREVIEWED' AND r.status IS NULL
        ) OR (
            r.status = :status
        ))
        ORDER BY COALESCE(r.lastUpdated, e.updateDateTime) DESC
    """)
    List<BlockParsingErrorReviewDTO> findAllBlockParsingErrorsByReviewStatus(@Param("status") ReviewStatus status, Pageable p);

    @Query("""
        SELECT new org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO(
            e.id,
            e.block,
            e.errorCode,
            e.reason,
            e.details,
            CASE
                WHEN r.status IS NULL THEN 'UNREVIEWED'
                ELSE r.status
            END,
            r.comment,
            r.checkedBy,
            'Please review all transactions within a block, https://explorer.cardano.org/block/' || e.block,
            COALESCE(r.lastUpdated, e.updateDateTime)
        )
        FROM ErrorEntity e
        LEFT JOIN ErrorReviewEntity r ON e.id = r.id
        WHERE e.block = :blockNumber
        ORDER BY COALESCE(r.lastUpdated, e.updateDateTime) DESC
    """)
    List<BlockParsingErrorReviewDTO> findAllBlockParsingErrorsByBlockNumber(@Param("blockNumber") long blockNumber, Pageable p);

}
