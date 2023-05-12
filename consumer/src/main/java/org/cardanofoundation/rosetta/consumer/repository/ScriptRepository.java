package org.cardanofoundation.rosetta.consumer.repository;

import java.util.List;
import java.util.Set;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.consumer.projection.ScriptProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {


  @Query("SELECT s.hash as hash,"
      + " s.id as id"
      + " FROM Script as s"
      + " WHERE s.hash IN (:hashes)")
  @Transactional(readOnly = true)
  List<ScriptProjection> getScriptByHashes(@Param("hashes") Set<String> hashes);
}
