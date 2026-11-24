package org.cardanofoundation.rosetta.api.error.model.repository;

import com.bloxbean.cardano.yaci.store.core.storage.impl.model.ErrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorRepository extends JpaRepository<ErrorEntity, Integer> {

    List<ErrorEntity> findTop1000ByOrderByUpdateDateTimeDesc();

}
