package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.springframework.beans.factory.annotation.Autowired;
import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.jetbrains.annotations.NotNull;
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

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

class BlockToBlockResponseTest extends BaseMapperSetup {


  @Autowired
  private BlockToBlockResponse my;

  @Test
  void toDto_test_Ok() {

    //given
    Block from = newBlock();

    //when
    BlockResponse into = my.toDto(newBlock());
    BlockResponse into1 = my.toDto(newBlock());
    BlockResponse into2 = my.toDto(newBlock());
    BlockResponse into3 = my.toDto(newBlock());
    BlockResponse into4 = my.toDto(newBlock());
    BlockResponse into5 = my.toDto(newBlock());
    BlockResponse into6 = my.toDto(newBlock());
    BlockResponse into7 = my.toDto(newBlock());
    BlockResponse into8 = my.toDto(newBlock());
    BlockResponse into9 = my.toDto(newBlock());
    BlockResponse into10 = my.toDto(newBlock());
    BlockResponse into11 = my.toDto(newBlock());
    BlockResponse into12 = my.toDto(newBlock());
    BlockResponse into13 = my.toDto(newBlock());
    BlockResponse into14 = my.toDto(newBlock());
    BlockResponse into15 = my.toDto(newBlock());
    BlockResponse into16 = my.toDto(newBlock());
    BlockResponse into17 = my.toDto(newBlock());
    BlockResponse into18 = my.toDto(newBlock());
    BlockResponse into19 = my.toDto(newBlock());
    BlockResponse into20 = my.toDto(newBlock());
    BlockResponse into21 = my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());
    my.toDto(newBlock());








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
    assertThat(from.getSlotNo()).isEqualTo(into.getBlock().getMetadata().getSlotNo());

    assertThat(from.getTransactions()).hasSameSizeAs(into.getBlock().getTransactions());
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
          assertAllPropertiesIsNull(d, "depositAmount");
          assertProperty(d, "depositAmount",
              Amount
                  .builder()
                  .currency(ada)
                  .value("500")
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
              Amount.builder().currency(ada).value("500").build());

          //d == List<PoolRegistrationParams> size == 2
          assertProperty(List.of(d.getFirst()), "poolRegistrationParams",
              buildRegParams(aiPool.incrementAndGet()));
          assertProperty(List.of(d.getLast()), "poolRegistrationParams",
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
        .allSatisfy(d ->
        {
          assertAllPropertiesIsNull(d, "poolKeyHash");
          assertProperty(List.of(d.getFirst()), "poolKeyHash",
              "poolDlg" + poolKeyHash.incrementAndGet());
          assertProperty(List.of(d.getLast()), "poolKeyHash",
              "poolDlg" + poolKeyHash.incrementAndGet());
        });


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
        4,
        "createdAt",
        4,
        6L, newTransactions(),
        "500");
  }

  private List<BlockTx> newTransactions() {
    return List.of(
        new BlockTx("hash1", "blockHash1", 1L, "fee1", 1L,
            1L, null, null,
            newStakeRegistrations(1, 2), newDelegations(1, 2), newPoolRegistrations(1, 2),
            newPoolRetirements(1, 2), null),

        new BlockTx("hash2", "blockHash2", 2L, "fee2", 2L,
            2L, null, null,
            newStakeRegistrations(3, 4), newDelegations(3, 4), newPoolRegistrations(3, 4),
            newPoolRetirements(3, 4), null),

        new BlockTx("hash3", "blockHash3", 3L, "fee3", 3L,
            3L, null, null,
            newStakeRegistrations(5, 6), newDelegations(5, 6), newPoolRegistrations(5, 6),
            newPoolRetirements(5, 6), null));
  }

  private List<Delegation> newDelegations(int... instances) {
    return Arrays.stream(instances)
        .mapToObj(ver -> Delegation.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .poolId("poolDlg" + ver)
            .address("delegationAcc" + ver)
            .build())
        .collect(Collectors.toList());
  }

  private List<PoolRegistration> newPoolRegistrations(int... instances) {
    return Arrays.stream(instances)
        .mapToObj(ver -> PoolRegistration.builder()
            .txHash("txHash" + ver)
            .poolId("poolReg" + ver)
            .vrfKeyHash("vrfKeyHash" + ver)
            .pledge("pledge" + ver)
            .cost("1" + ver)
            .margin("1.0" + ver)
            .rewardAccount("rewardAccount" + ver)
            .owners(newOwners(ver))
            .relays(List.of(Relay.builder().ipv4("ipv4" + ver).ipv6("ipv6" + ver)
                .dnsName("dnsName" + ver).port(1 + ver).type("type" + ver).build()))
            .build())
        .collect(Collectors.toList());
  }

  @NotNull
  private static Set<String> newOwners(int ver) {
    TreeSet<String> treeSet = new TreeSet<>();
    treeSet.add("owner1" + ver);
    treeSet.add("owner2" + ver);
    return treeSet;
  }


  private List<PoolRetirement> newPoolRetirements(int... instances) {
    return Arrays.stream(instances)
        .mapToObj(ver -> PoolRetirement.builder()
            .txHash("txHash" + ver)
            .poolId("poolRet" + ver)
            .epoch(1 + ver)
            .build())
        .collect(Collectors.toList());
  }

  private List<StakeRegistration> newStakeRegistrations(int... instances) {

    return Arrays.stream(instances)
        .mapToObj(ver -> StakeRegistration.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .type(CertificateType.STAKE_REGISTRATION)
            .address("address" + ver)
            .build())
        .collect(Collectors.toList());


  }
}
