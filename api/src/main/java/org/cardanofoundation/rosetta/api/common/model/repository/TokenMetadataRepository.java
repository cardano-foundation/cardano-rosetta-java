package org.cardanofoundation.rosetta.api.common.model.repository;

import org.cardanofoundation.rosetta.api.common.model.entity.TokenMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenMetadataRepository extends JpaRepository<TokenMetadataEntity, String> {

    List<TokenMetadataEntity> findAllBySubjectIn(List<String> subjects);
}
