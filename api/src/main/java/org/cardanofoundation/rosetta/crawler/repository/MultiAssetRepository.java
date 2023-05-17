package org.cardanofoundation.rosetta.crawler.repository;

import org.cardanofoundation.rosetta.common.entity.MultiAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultiAssetRepository extends JpaRepository<MultiAsset, Long> {

}