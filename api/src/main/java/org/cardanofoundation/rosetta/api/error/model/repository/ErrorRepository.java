package org.cardanofoundation.rosetta.api.error.model.repository;

import org.cardanofoundation.rosetta.api.error.model.domain.ErrorDTO;

import java.util.List;
import java.util.Optional;

public interface ErrorRepository {

    boolean hasErrors();

    List<ErrorDTO> findMostRecentErrors();

    Optional<ErrorDTO> findById(Integer id);

}
