package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.RedeemerData;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RedeemerDataRepository extends JpaRepository<RedeemerData, Long> {

  @Transactional(readOnly = true)
  List<RedeemerData> findAllByHashIn(Collection<String> hashes);
}
