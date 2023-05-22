package org.cardanofoundation.rosetta.api.repository;


import org.cardanofoundation.rosetta.api.projection.EpochParamProjection;
import org.cardanofoundation.rosetta.common.entity.EpochParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EpochParamRepository extends JpaRepository<EpochParam, Long> {
  @Query(value = "SELECT coinsPerUtxoSize as coinsPerUtxoSize, "
      + "maxTxSize as maxTxSize, "
      + "maxValSize as maxValSize, "
      + "keyDeposit as keyDeposit, "
      + "maxCollateralInputs as maxCollateralInputs, "
      + "minFeeA as minFeeA, "
      + "minFeeB as minFeeB, "
      + "minPoolCost as minPoolCost, "
      + "poolDeposit as poolDeposit, "
      + "protocolMajor as protocolMajor "
      + "FROM EpochParam ORDER BY id DESC",
      countQuery = "select count(id) FROM EpochParam ")
  Page<EpochParamProjection> findProtocolParameters(Pageable pageable);
}