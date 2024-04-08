package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.springframework.beans.factory.annotation.Autowired;
import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.Relay;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

class BlockToBlockResponseTest extends BaseMapperTest {


  @Autowired
  private BlockToBlockResponse my;

  @Test
  void toDto_test_Ok() {

    //given
    Block from = newBlock();

    //when
    BlockResponse into = my.toDto(newBlock());

    //then
    assertThat(from.getHash()).isEqualTo(into.getBlock().getBlockIdentifier().getHash());
    assertThat(from.getNumber()).isEqualTo(into.getBlock().getBlockIdentifier().getIndex());

    assertThat(from.getPreviousBlockHash()).isEqualTo(
        into.getBlock().getParentBlockIdentifier().getHash());

    assertThat(from.getPreviousBlockNumber()).isEqualTo(
        into.getBlock().getParentBlockIdentifier().getIndex());

    assertThat(from.getCreatedAt()).isEqualTo(into.getBlock().getTimestamp());

    assertThat(from.getTransactionsCount()).isEqualTo(
        into.getBlock().getMetadata().getTransactionsCount());
    assertThat(from.getCreatedBy()).isEqualTo(into.getBlock().getMetadata().getCreatedBy());
    assertThat(from.getSize()).isEqualTo(into.getBlock().getMetadata().getSize());
    assertThat(from.getEpochNo()).isEqualTo(into.getBlock().getMetadata().getEpochNo());
    assertThat(from.getSlotNo()).isEqualTo(into.getBlock().getMetadata().getSlotNo());

    assertThat(from.getTransactions().size()).isEqualTo(
        into.getBlock().getTransactions().size());
    assertThat(from.getTransactions())
        .extracting(BlockTx::getHash)
        .isEqualTo(into.getBlock().getTransactions().stream()
            .map(t -> t.getTransactionIdentifier().getHash()).toList());

    assertThat(from.getTransactions())
        .extracting(BlockTx::getSize)
        .isEqualTo(into.getBlock().getTransactions().stream()
            .map(t -> t.getMetadata().getSize()).toList());

    assertThat(from.getTransactions())
        .extracting(BlockTx::getScriptSize)
        .isEqualTo(into.getBlock().getTransactions().stream()
            .map(t -> t.getMetadata().getScriptSize()).toList());

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations().size())
        .isEqualTo(List.of(8, 8, 8));

    List<String> sts = IntStream.range(0, 8)
        .mapToObj(p -> SUCCESS_OPERATION_STATUS.getStatus())
        .collect(Collectors.toList());
    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getStatus)
            .collect(Collectors.toList()))
        .containsAnyOf(sts);

    List<OperationIdentifier> opIds = LongStream.range(0, 8)
        .mapToObj(p -> OperationIdentifier
            .builder()
            .index(p)
            .build())
        .toList();
    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getOperationIdentifier)
            .collect(Collectors.toList()))
        .containsAnyOf(opIds);

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getRelatedOperations)
            .collect(Collectors.toList()))
        .extracting(p -> p == null ? Collections.emptyList() : p)
        .allSatisfy(
            BlockToBlockResponseTest::assertAllElementsIsNull);

    List<String> types = List.of(
        "stakeKeyRegistration", "stakeKeyRegistration",
        "stakeDelegation", "stakeDelegation",
        "poolRegistration", "poolRegistration",
        "poolRetirement", "poolRetirement");
    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getType)
            .collect(Collectors.toList()))
        .containsAnyOf(types);

    List<AccountIdentifier> accIds = List.of(
        newAccId("address1"), newAccId("address2"),
        newAccId("delegationAcc1"), newAccId("delegationAcc2"),
        newAccId("poolReg1"), newAccId("poolReg2"),
        newAccId("poolRet1"), newAccId("poolRet2"));
    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getAccount)
            .collect(Collectors.toList()))
        .containsAnyOf(accIds);

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getCoinChange)
            .collect(Collectors.toList()))
        .allSatisfy(BlockToBlockResponseTest::assertAllElementsIsNull);

    Currency ada = Currency
        .builder()
        .symbol("ADA")
        .decimals(6)
        .metadata(null)
        .build();

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .filter(
                g -> g.getType().equals("stakeKeyRegistration"))
            .map(Operation::getMetadata)
            .collect(Collectors.toList()))
        .allSatisfy(d -> {
          //TODO saa: is it OK to have all values here is null other than depositAmount?
          assertAllPropertiesIsNull(d, "depositAmount");
          assertProperty(d, "depositAmount",
              Amount
                  .builder()
                  .currency(ada)
                  .value("2000000")
                  .build());
        });

    AtomicInteger aiPool = new AtomicInteger(0); //just immutable helper

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .filter(
                g -> g.getType().equals("poolRegistration"))
            .map(Operation::getMetadata)
            .collect(Collectors.toList()))
        .allSatisfy(d -> {

          assertAllPropertiesIsNull(d, "depositAmount", "poolRegistrationParams");

          assertProperty(d, "depositAmount",
              Amount.builder().currency(ada).value("5000").build());

          //d == List<PoolRegistrationParams> size == 2
          assertProperty(List.of(orderOwners(d.getFirst())), "poolRegistrationParams",
              buildRegParams(aiPool.incrementAndGet()));
          assertProperty(List.of(orderOwners(d.getLast())), "poolRegistrationParams",
              buildRegParams(aiPool.incrementAndGet()));
        });

    AtomicInteger aiEpoch = new AtomicInteger(1); //just immutable helper
    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .filter(
                g -> g.getType().equals("poolRetirement"))
            .map(Operation::getMetadata)
            .collect(Collectors.toList()))
        .allSatisfy(d -> {
          assertAllPropertiesIsNull(d, "epoch");
          assertProperty(List.of(d.getFirst()), "epoch", aiEpoch.incrementAndGet());
          assertProperty(List.of(d.getLast()), "epoch", aiEpoch.incrementAndGet());
        });

    AtomicInteger poolKeyHash = new AtomicInteger(0); //just immutable helper
    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .filter(
                g -> g.getType().equals("stakeDelegation"))
            .map(Operation::getMetadata)
            .collect(Collectors.toList()))
        .allSatisfy(d->
        {
          assertAllPropertiesIsNull(d, "poolKeyHash");
          assertProperty(List.of(d.getFirst()), "poolKeyHash", "poolDlg"+poolKeyHash.incrementAndGet());
          assertProperty(List.of(d.getLast()), "poolKeyHash", "poolDlg"+poolKeyHash.incrementAndGet());
        });


  }

  private OperationMetadata orderOwners(OperationMetadata om) { //TODO saa rewrite with TreeSet

//    getPoolOwners() -- immutable list, so need to convert to array and back
    String[] poolOwners = om.getPoolRegistrationParams().getPoolOwners().toArray(new String[]{});
    Arrays.sort(poolOwners); //sort required because of the Set in domain model:
    // - org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration.owners
    om.getPoolRegistrationParams().setPoolOwners(List.of(poolOwners));
    return om;
  }

  private static PoolRegistrationParams buildRegParams(int i) {
    return PoolRegistrationParams.builder()
        .vrfKeyHash("vrfKeyHash" + i)
        .pledge("pledge" + i)
        .cost("1" + i)
        .margin(null)
        .rewardAddress("rewardAccount" + i)
        .marginPercentage("1.0" + i)
        .poolOwners(List.of("owner1" + i, "owner2" + i))
        .relays(List.of(Relay
            .builder().ipv4("ipv4" + i).ipv6("ipv6" + i)
            .dnsName("dnsName" + i).port(1 + i).type("type" + i).build()))
        .build();
  }


  private static void assertAllPropertiesIsNull(List<OperationMetadata> g, String... except) {
    g.forEach(m -> assertThat(m).hasAllNullFieldsOrPropertiesExcept(except));
  }

  private static void assertProperty(List<OperationMetadata> g, String prop, Object actual) {
    g.forEach(m -> assertThat(m)
        .extracting(prop)
        .isEqualTo(actual));

  }

  private static <T> void assertAllElementsIsNull(List<T> g) {
    g.forEach(m -> assertThat(m).isNull());
  }

  private static AccountIdentifier newAccId(String address) {
    return AccountIdentifier.builder()
        .address(address)
        .subAccount(null)
        .metadata(null)
        .build();
  }

  private Block newBlock() {
    return new Block(
        "hash",
        1L,
        2L,
        "prevHashBlock",
        21L,
        3L,
        "createdAt",
        4, 5,
        6L, newTransactions(),
        "5000");
  }

  private List<BlockTx> newTransactions() {
    return List.of(new BlockTx("hash1", "blockHash1", 1L, "fee1", 1L,
            true, 1L, null, null,
            newStakeRegistrations(1, 2), newDelegations(1, 2), newPoolRegistrations(1, 2),
            newPoolRetirements(1, 2)),

        new BlockTx("hash2", "blockHash2", 2L, "fee2", 2L,
            true, 2L, null, null,
            newStakeRegistrations(3, 4), newDelegations(3, 4), newPoolRegistrations(3, 4),
            newPoolRetirements(3, 4)),

        new BlockTx("hash3", "blockHash3", 3L, "fee3", 3L,
            true, 3L, null, null,
            newStakeRegistrations(5, 6), newDelegations(5, 6), newPoolRegistrations(5, 6),
            newPoolRetirements(5, 6)));
  }

  private List<Delegation> newDelegations(int... instances) {
    return Arrays.stream(instances)
        .mapToObj(ver -> Delegation.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .credential("credential" + ver)
            .epoch(1 + ver)
            .slot(1L + ver)
            .poolId("poolDlg" + ver)
            .address("delegationAcc" + ver)
            .blockHash("blockHash" + ver)
            .build())
        .collect(Collectors.toList());
  }

  private List<PoolRegistration> newPoolRegistrations(int... instances) {
    return Arrays.stream(instances)
        .mapToObj(ver -> PoolRegistration.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .poolId("poolReg" + ver)
            .vrfKeyHash("vrfKeyHash" + ver)
            .pledge("pledge" + ver)
            .cost("1" + ver)
            .margin("1.0" + ver)
            .rewardAccount("rewardAccount" + ver)
            .owners(Set.of("owner1" + ver, "owner2" + ver))
            .relays(List.of(Relay.builder().ipv4("ipv4" + ver).ipv6("ipv6" + ver)
                .dnsName("dnsName" + ver).port(1 + ver).type("type" + ver).build()))
            .epoch(1 + ver)
            .slot(1L + ver)
            .blockHash("blockHash" + ver)
            .build())
        .collect(Collectors.toList());
  }


  private List<PoolRetirement> newPoolRetirements(int... instances) {
    return Arrays.stream(instances)
        .mapToObj(ver -> PoolRetirement.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .poolId("poolRet" + ver)
            .epoch(1 + ver)
            .slot(1L + ver)
            .blockHash("blockHash" + ver)
            .build())
        .collect(Collectors.toList());
  }

  private List<StakeRegistration> newStakeRegistrations(int... instances) {

    return Arrays.stream(instances)
        .mapToObj(ver -> StakeRegistration.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .credential("credential" + ver)
            .type(CertificateType.STAKE_REGISTRATION)
            .address("address" + ver)
            .epoch(1 + ver)
            .slot(1L + ver)
            .blockHash("blockHash" + ver)
            .build())
        .collect(Collectors.toList());


  }


}


