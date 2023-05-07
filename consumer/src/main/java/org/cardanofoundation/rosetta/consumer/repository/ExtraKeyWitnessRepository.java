package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.ExtraKeyWitness;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExtraKeyWitnessRepository extends JpaRepository<ExtraKeyWitness, Long> {


  @Query("SELECT e.hash FROM ExtraKeyWitness e WHERE e.hash IN :collection ")
  Set<String> findByHashIn(@Param("collection") Collection<String> collection);
}
