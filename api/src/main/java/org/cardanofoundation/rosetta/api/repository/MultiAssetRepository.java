package org.cardanofoundation.rosetta.api.repository;

import org.cardanofoundation.rosetta.common.entity.MultiAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultiAssetRepository extends JpaRepository<MultiAsset, Long> {

}