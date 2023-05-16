package org.cardanofoundation.rosetta.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.consumer.repository.*;
import org.cardanofoundation.rosetta.consumer.service.impl.GenesisDataServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GenesisServiceTest {
    @Value("${genesis.file}")
    String fileString;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    GenesisDataServiceImpl genesisDataService;
    @Autowired
    BlockRepository blockRepository;
    @Autowired
    SlotLeaderRepository slotLeaderRepository;
    @Autowired
    TxRepository txRepository;
    @Autowired
    TxOutRepository txOutRepository;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    AddressTxBalanceRepository addressTxBalanceRepository;
    @Autowired
    CostModelRepository costModelRepository;

    @Test
    void testInsertGenesis() {
        genesisDataService.test();
        genesisDataService.setupData();
//        Assertions.assertEquals(1, blockRepository.findAll().size());
//        Assertions.assertEquals(8, txRepository.findAll().size());
//        Assertions.assertEquals(8, txOutRepository.findAll().size());
//        Assertions.assertEquals(1, slotLeaderRepository.findAll().size());
//        Assertions.assertEquals(1, costModelRepository.findAll().size());
//        Assertions.assertEquals(8, addressRepository.findAll().size());
//        Assertions.assertEquals(8, addressTxBalanceRepository.findAll().size());
        System.out.println("Done");
    }
}
