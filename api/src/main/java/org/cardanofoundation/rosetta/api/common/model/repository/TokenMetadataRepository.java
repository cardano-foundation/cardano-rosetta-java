package org.cardanofoundation.rosetta.api.common.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.common.model.entity.TokenMetadataEntity;

@Repository
public interface TokenMetadataRepository extends JpaRepository<TokenMetadataEntity, String> {

    List<TokenMetadataEntity> findAllBySubjectIn(List<String> subjects);
}
