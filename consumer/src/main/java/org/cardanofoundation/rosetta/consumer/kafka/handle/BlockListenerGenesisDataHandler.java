package org.cardanofoundation.rosetta.consumer.kafka.handle;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.service.GenesisDataService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Profile("!test-integration")
@Slf4j
public class BlockListenerGenesisDataHandler {

  final GenesisDataService genesisDataService;
  final BlockRepository blockRepository;

  @PostConstruct
  void setupGenesisData() {
    int sizeBlock = blockRepository.findAll().size();
    if (sizeBlock == 0){
      log.info("Genesis data");
      genesisDataService.setupData();
    }
  }
}
