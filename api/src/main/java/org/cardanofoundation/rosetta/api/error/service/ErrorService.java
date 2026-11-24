package org.cardanofoundation.rosetta.api.error.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bloxbean.cardano.yaci.store.core.storage.impl.model.ErrorEntity;
import org.cardanofoundation.rosetta.api.error.model.repository.ErrorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ErrorService {

    private final ErrorRepository errorRepository;

    public Optional<ErrorEntity> findById(Integer id) {
        return errorRepository.findById(id);
    }

}
