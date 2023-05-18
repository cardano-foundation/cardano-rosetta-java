package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.crawler.service.LedgerDataProviderService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class LedgerDataProviderServiceTest {
    @Autowired
    private LedgerDataProviderService ledgerDataProviderService;

    @Test
    void getTip() {
    }

    @Test
    void findGenesisBlock() {
        ledgerDataProviderService.findGenesisBlock();
    }

    @Test
    void findBlock() {
    }

    @Test
    void findBalanceByAddressAndBlock() {
    }

    @Test
    void findUtxoByAddressAndBlock() {
    }

    @Test
    void findLatestBlockNumber() {
    }
}