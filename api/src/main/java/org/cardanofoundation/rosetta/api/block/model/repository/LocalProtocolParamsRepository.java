package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.cardanofoundation.rosetta.api.block.model.entity.LocalProtocolParamsEntity;

public interface LocalProtocolParamsRepository extends JpaRepository<LocalProtocolParamsEntity, Long> {

    @Query(value = """
            SELECT p FROM LocalProtocolParamsEntity p ORDER BY p.epoch DESC LIMIT 1
            """
    )
    Optional<LocalProtocolParamsEntity> getLocalProtocolParams();
}
