package org.cardanofoundation.rosetta.api.error.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.error.model.domain.ErrorDTO;
import org.cardanofoundation.rosetta.api.error.model.repository.ErrorRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ErrorService {

    private final ErrorRepository errorRepository;

    public boolean hasAnyError() {
        return errorRepository.hasErrors();
    }

    public Optional<ErrorDTO> findById(Integer id) {
        return errorRepository.findById(id);
    }

    public List<ErrorDTO> findMostRecentErrors() {
        return errorRepository.findMostRecentErrors();
    }

    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes, at 0th second
    public void notifyErrors() {
        if (hasAnyError()) {
            log.warn("Block parsing errors found in the database, please check if you are affected via endpoint: /call/."); // TODO name of the endpoint
        }
    }

}
