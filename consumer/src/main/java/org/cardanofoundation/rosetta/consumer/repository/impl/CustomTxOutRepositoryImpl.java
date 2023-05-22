package org.cardanofoundation.rosetta.consumer.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.cardanofoundation.rosetta.common.entity.BaseEntity_;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.common.entity.TxOut_;
import org.cardanofoundation.rosetta.common.entity.Tx_;
import org.cardanofoundation.rosetta.consumer.projection.TxOutProjection;
import org.cardanofoundation.rosetta.consumer.repository.CustomTxOutRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CustomTxOutRepositoryImpl implements CustomTxOutRepository {

  @PersistenceContext
  EntityManager entityManager;

  @Override
  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  public Set<TxOutProjection> findTxOutsByTxHashInAndTxIndexIn(
      List<Pair<String, Short>> txHashIndexPairs) {
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var txOutQuery = criteriaBuilder.createQuery(TxOutProjection.class);
    Root<TxOut> txOutRoot = txOutQuery.from(TxOut.class);
    Join<TxOut, Tx> tx = txOutRoot.join(TxOut_.tx);
    Join<TxOut, StakeAddress> stakeAddress = txOutRoot.join(TxOut_.stakeAddress, JoinType.LEFT);
    var txOutSelection = buildTxOutSelectQuery(criteriaBuilder, txOutRoot, tx, stakeAddress);

    Predicate[] predicates = txHashIndexPairs.stream()
        .map(txHashIndexPair ->
            buildTxOutPredicate(criteriaBuilder, txOutRoot, tx, txHashIndexPair))
        .toArray(Predicate[]::new);

    txOutQuery.select(txOutSelection).where(criteriaBuilder.or(predicates));
    return entityManager.createQuery(txOutQuery).getResultStream().collect(Collectors.toSet());
  }

  private static Predicate buildTxOutPredicate(
      CriteriaBuilder criteriaBuilder, Root<TxOut> txOutRoot, Join<TxOut, Tx> tx,
      Pair<String, Short> txHashIndexPair) {
    String txHash = txHashIndexPair.getFirst();
    int index = txHashIndexPair.getSecond();
    Predicate txHashEquals = criteriaBuilder.equal(tx.get(Tx_.hash), txHash);
    Predicate indexEquals = criteriaBuilder.equal(txOutRoot.get(TxOut_.index), index);

    return criteriaBuilder.and(txHashEquals, indexEquals);
  }

  private static CompoundSelection<TxOutProjection> buildTxOutSelectQuery(
      CriteriaBuilder criteriaBuilder, Root<TxOut> txOutRoot,
      Join<TxOut, Tx> tx, Join<TxOut, StakeAddress> stakeAddress) {
    return criteriaBuilder.construct(
        TxOutProjection.class,
        txOutRoot.get(BaseEntity_.id),
        tx.get(Tx_.hash),
        tx.get(BaseEntity_.id),
        txOutRoot.get(TxOut_.index),
        txOutRoot.get(TxOut_.value),
        stakeAddress.get(BaseEntity_.id),
        txOutRoot.get(TxOut_.address),
        txOutRoot.get(TxOut_.addressHasScript),
        txOutRoot.get(TxOut_.paymentCred)
    );
  }
}
