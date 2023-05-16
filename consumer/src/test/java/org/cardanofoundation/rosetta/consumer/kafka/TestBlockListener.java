package org.cardanofoundation.rosetta.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.cardanofoundation.rosetta.common.ledgersync.*;
import org.cardanofoundation.rosetta.common.ledgersync.kafka.CommonBlock;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.CardanoRosettaConsumerApplication;
import org.cardanofoundation.rosetta.consumer.factory.BlockAggregatorServiceFactory;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedBlockRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.BlockSyncService;
import org.cardanofoundation.rosetta.consumer.service.RollbackService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Base64;

@SpringBootTest(classes = CardanoRosettaConsumerApplication.class)
public class TestBlockListener {

//    @Autowired
//    BlockListener blockListener;
//
//    @MockBean
//    private BlockAggregatorServiceFactory aggregatorServiceFactory;
//
//    @MockBean
//    private BlockSyncService blockSyncService;
//
//    @MockBean
//    private BlockDataService blockDataService;
//
//    @MockBean
//    private RollbackService rollbackService;
//
//    @MockBean
//    private CachedBlockRepository cachedBlockRepository;
//
//    @MockBean
//    private BlockRepository blockRepository;
//
//    @Test
//    public void consumer(){
//        Assertions.assertEquals("2","2");
//
//        String topic = "preprod.crawler.blocks";
//        int partition = 0;
//        long offset = 19002;
//        String key = "095e6abe7aa550a1b33c92cd571122c768abd81c90f1610441873459f31aeb73";
//
//        Epoch epoch = new Epoch(4,379120,465520);
//        VrfCert nonceVrfCert = new VrfCert("836fdd1c2cba435cc785b60bc0d421117a87bb90cccee9f9577e7e13953280bd4b5ac5ce830e0fa97a49c39718d6e7565f3202be516157e4b1aeb3b3429dc500","fc6be527985be70bfea1f0962accc50e41d032ad291b41964fbc6e43b0f19d45f13e8123c576562f9ecabeb521369371c7185de1ab872409e1efed6c24ec3ede58d1a58579eee568bac904dbd2f9980a");
//        VrfCert leaderVrfCert = new VrfCert("3a605551c77fe09059595592d9d00211776d863a12ec0d70888bbce9d269efd2bb69fde4b0821feb6ac8d7117b1917c7a63af688357fdc15c7808fd62d3891cd","d4027feb68af82927acb49d8578dd223b2fa0f2490d8958fb2637f7b782d37f552f14a10c9be6cb63bb5c87612b2ef7dd2f36e57ff57e5a176072c4bf1a19defd246179f1ac85382a8d0ff207e243104");
//        ProtocolVersion protocolVersion = new ProtocolVersion(3,0);
//        OperationalCert operationalCert = OperationalCert.builder()
//                .hotKey("2b9a5add912f3edc5c325d6250b9cc154de8f35e2924f5b1c707a4123808d064")
//                .sequenceNumber(0)
//                .kesPeriod(0)
//                .sigma("7fb060b885ffc7b55bb6e095ea6999eaa5608b6b4e92f5cc64bc34ba85000ebe42839432f15d86df07740374f69ce60f72feb16a0d47f5cf2c43158caf37ad03")
//                .build();
//
//        HeaderBody headerBody = HeaderBody.builder()
//                .blockNumber(19002)
//                .slotId(epoch)
//                .prevHash("74534bf4635280822f457e03a9839897d28345ce72cd3e26a18b06f1b3047a2a")
//                .issuerVkey("d1a8de6caa8fd9b175c59862ecdd5abcd0477b84b82a0e52faecc6b3c85100a4")
//                .vrfVkey("51995f616f8a025f974b20330a53c0c81e8ea95973d73d15fff7bab57589311d")
//                .nonceVrf(nonceVrfCert)
//                .leaderVrf(leaderVrfCert)
//                .vrfResult(null)
//                .blockBodySize(3)
//                .blockBodyHash("1033376be025cb705fd8dd02eda11cc73975a062b5d14ffd74d6ff69e69a2ff7")
//                .operationalCert(operationalCert)
//                .protocolVersion(protocolVersion)
//                .blockHash("095e6abe7aa550a1b33c92cd571122c768abd81c90f1610441873459f31aeb73")
//                .build();
//
//        BlockHeader blockHeader = new BlockHeader(headerBody,"c4353df9ff7f431b0d0956329a1442a25e154b748db1c3004ef0257db0556869870b604e7633f2c967aeb1000b4af7e91ef8d1921e1867c6d7f4ff416e0e7a04b1959cc8ecac05e185d1e133b8217e90bb6c9632f6ad36782ec20368a2412b56336723e3ff73624fce15fd01f9c4b39bf158ac3acd597a625225fd6a0d4873f5d57538a7b9d64f2defee3e32879e36db649a934b00784e6223023bdfffa59f4e54609d63a6f5ad04850c419a3556db8b291b90467fadfc67194a3069ef6ff4c0f7d6677145ceb51be68d6d0c20d0e92f80313c48dabf5ae8e3acd9fc43f450874848221f71d2f895c18790082d17467de32ff047a22cee1799db7e77e651a35c15b32d4f838133cc80d467308587ff5cea12be5b3b8b7d2d0d2eadf066b67cd965100555f96457d0d70988ffc2a7c212afa73338df3ece84ee7de2170aadec1dafc360580432193ab2a25c9c4555e57bc0d88cf50d7036378b4dabde79e5f858539a464e0a547660374da91d7d19acd753e219a8fee41a43bd4190db235dc0b1224bcfb9a760fb2b39063dccce88453043c0297cb6c93bca145a9ebbd6bc3a916ed9439343ac3510c47886d17a9187e833b9149e5ac2854c4d88a7c4b4ee6882");
//        Block block = new Block();
//        block.setHeader(blockHeader);
//        block.setBlockTime(1656148720);
//        block.setNetwork(1);
//        block.setRollback(false);
//        block.setCborSize(1006);
//        block.setEraType(Era.SHELLEY);
//
//        ConsumerRecord<String, CommonBlock> consumerRecord = new ConsumerRecord<>(topic, partition, offset, key, block);
//        blockListener.consume(consumerRecord,null);
//        boolean isExistsBlockByHash = blockRepository.existsBlockByHash("095e6abe7aa550a1b33c92cd571122c768abd81c90f1610441873459f31aeb73");
//        System.out.println(isExistsBlockByHash);
//        System.out.println(consumerRecord);
////        System.out.println(consumerRecord);
//
//
////        System.out.println("he");
//    }

    @Test
    public void toByte(){
        String rawString = "gtgYWCGDWBzJm/Ck1uI14t8kS5CqVd0ELfQ/kJjmO6n55QV/oAIanmvCVQ==";
        byte[] addressBytes = Base64.getDecoder().decode(rawString);
        String addressHex = HexUtil.encodeHexString(addressBytes);
        System.out.println(addressHex);
    }
}
