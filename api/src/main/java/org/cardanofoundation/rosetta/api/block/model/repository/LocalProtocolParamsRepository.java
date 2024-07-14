package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.LocalProtocolParamsEntity;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LocalProtocolParamsRepository extends JpaRepository<LocalProtocolParamsEntity, Long> {

    @Query(value = """
            SELECT p FROM LocalProtocolParamsEntity p ORDER BY p.id DESC LIMIT 1
            """
    )
    Optional<LocalProtocolParamsEntity> getLocalProtocolParams();
}
