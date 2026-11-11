package org.cardanofoundation.rosetta.api.common.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.common.model.entity.Cip26MetadataEntity;

/**
 * Read-only access to yaci-store assets-ext {@code cip26_metadata}.
 */
@Repository
public interface Cip26MetadataRepository extends JpaRepository<Cip26MetadataEntity, String> {

    List<Cip26MetadataEntity> findAllBySubjectIn(List<String> subjects);

}
