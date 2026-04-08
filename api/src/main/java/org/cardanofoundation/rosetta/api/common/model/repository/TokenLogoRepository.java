package org.cardanofoundation.rosetta.api.common.model.repository;

import org.cardanofoundation.rosetta.api.common.model.entity.TokenLogoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenLogoRepository extends JpaRepository<TokenLogoEntity, String> {

    List<TokenLogoEntity> findAllBySubjectIn(List<String> subjects);
}
