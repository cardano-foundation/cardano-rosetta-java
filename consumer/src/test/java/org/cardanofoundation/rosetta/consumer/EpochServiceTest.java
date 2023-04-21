package org.cardanofoundation.rosetta.consumer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

@SpringBootTest
@Profile("test")
class EpochServiceTest {

//  @Autowired
//  private EpochServiceImpl epochService;
//  @Autowired
//  private BlockSyncServiceImpl blockSyncService;
//  @Autowired
//  private EpochRepository epochRepository;
//  @Autowired
//  private BlockRepository blockRepository;
//  @Autowired
//  private SlotLeaderRepository slotLeaderRepository;
//  @Autowired
//  private PoolHashRepository poolHashRepository;
//
//  @Test
//  void insertShelley() {
//
//    com.sotatek.cardano.common.entity.Block block = new com.sotatek.cardano.common.entity.Block();
//    block.setHash("111");
//    block.setEpochNo(0);
//    block.setTime(Timestamp.valueOf(
//        LocalDateTime.ofEpochSecond(Instant.now().getEpochSecond(),
//            0,
//            ZoneOffset.UTC)));
//    /////// block 2
//
//    com.sotatek.cardano.common.entity.Block block1 = new com.sotatek.cardano.common.entity.Block();
//    block1.setHash("1111");
//    block1.setEpochNo(0);
//    Long expectSecond = Instant.now().getEpochSecond() + 20;
//    block1.setTime(
//        Timestamp.valueOf(
//            LocalDateTime.ofEpochSecond(expectSecond
//                , 0,
//                ZoneOffset.UTC)));
//
//    // Save Block
//
//    blockRepository.save(block);
//    blockRepository.save(block1);
//
//    Block blockCddl1 = Block.builder()
//        .header(BlockHeader.builder()
//            .headerBody(HeaderBody.builder()
//                .slotId(Epoch
//                    .builder()
//                    .slotId(0)
//                    .slotOfEpoch(0)
//                    .value(0)
//                    .build())
//                .blockHash("TienDD")
//                .blockBodyHash("xzxxx")
//                .blockBodySize(0)
//                .build())
//            .build())
//        .transactionBodies(new ArrayList<TransactionBody>())
//        .build();
//
//    blockCddl1.setBlockTime(Instant.now().getEpochSecond());
//
//    epochService.handleEpoch(blockCddl1);
//
//    Block blockCddl = Block.builder()
//        .header(BlockHeader.builder()
//            .headerBody(HeaderBody.builder()
//                .slotId(Epoch
//                    .builder()
//                    .slotId(0)
//                    .slotOfEpoch(0)
//                    .value(1)
//                    .build())
//                .build())
//            .build())
//        .transactionBodies(new ArrayList<TransactionBody>())
//        .build();
//
//    blockCddl.setBlockTime(Instant.now().getEpochSecond());
//    epochService.handleEpoch(blockCddl);
//
//    var epoch = epochRepository.findEpochByNo(BigInteger.ZERO.intValue());
//
//    Assertions.assertThat(
//            LocalDateTime.ofEpochSecond(expectSecond, 0, ZoneOffset.UTC))
//        .isEqualTo(epoch.get().getEndTime().toLocalDateTime());
//  }

}
