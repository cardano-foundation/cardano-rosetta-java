package org.cardanofoundation.rosetta.api.error.model.repository;

import org.cardanofoundation.rosetta.api.error.model.entity.ErrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorRepository extends JpaRepository<ErrorEntity, Integer> {

    List<ErrorEntity> findTop1000ByOrderByUpdateDateTimeDesc();

}
