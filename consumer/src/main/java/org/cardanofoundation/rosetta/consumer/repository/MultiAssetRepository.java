package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.MultiAsset;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MultiAssetRepository extends JpaRepository<MultiAsset, Long> {

  /*
   * Selected entities' changes are not meant to be reflected immediately in database
   * due to slow update time or any unexpected behaviour, hence this select query should
   * be marked as read-only transaction, as the changes will be applied later through
   * JPA save() or saveAll() methods
   */
  @Transactional(readOnly = true)
  List<MultiAsset> findMultiAssetsByFingerprintIn(Collection<String> fingerprints);

}
