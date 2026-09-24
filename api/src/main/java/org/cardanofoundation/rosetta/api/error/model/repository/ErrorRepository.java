package org.cardanofoundation.rosetta.api.error.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.rosetta.api.error.model.entity.ErrorEntity;

@Repository
public interface ErrorRepository extends JpaRepository<ErrorEntity, Integer> {

    List<ErrorEntity> findTop1000ByOrderByUpdateDateTimeDesc();

}
