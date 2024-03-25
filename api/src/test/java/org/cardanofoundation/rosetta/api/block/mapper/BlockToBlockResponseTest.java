package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.Relay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.Transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

class BlockToBlockResponseTest {

  private ModelMapper modelMapper;

  @BeforeEach
  void setUp() {
    modelMapper = new ModelMapper();
  }

  @Test
  void toDto_test_Ok() {

    //given
    BlockToBlockResponse my = new BlockToBlockResponse(modelMapper);
    my.modelMapper.validate();

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
        .extracting(Transaction::getHash)
        .isEqualTo(into.getBlock().getTransactions().stream()
            .map(t -> t.getTransactionIdentifier().getHash()).toList());

    assertThat(from.getTransactions())
        .extracting(Transaction::getSize)
        .isEqualTo(into.getBlock().getTransactions().stream()
            .map(t -> t.getMetadata().getSize()).toList());

    assertThat(from.getTransactions())
        .extracting(Transaction::getScriptSize)
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
            .networkIndex(null) //TODO saa: should networkIndex == null?
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
            BlockToBlockResponseTest::assertAllElementsIsNull); // TODO saa: is it OK to have all nulls for getRelatedOperations?

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
        newAccId(null), newAccId(null), //TODO saa: should be null?
        newAccId("poolId1"), newAccId("poolId2"), //TODO saa: repeated?
        newAccId("poolId1"), newAccId("poolId2"));
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
        .allSatisfy(BlockToBlockResponseTest::assertAllElementsIsNull); //TODO saa: return null?

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .filter( g-> g.getType().equals("stakeKeyRegistration") ) //TODO saa: add more filters !!!!
            .map(Operation::getMetadata)
            .collect(Collectors.toList()))
        .allSatisfy(BlockToBlockResponseTest::assertAllPropertiesIsNull); //TODO saa: return null?


  }


  private static void assertAllPropertiesIsNull(List<OperationMetadata> g) {
    g.forEach(m -> assertThat(m)

        .hasAllNullFieldsOrPropertiesExcept(
            "depositAmount", "poolRegistrationParams", "epoch"));
    g.forEach(m -> assertThat(m.getDepositAmount())
        .isEqualTo(Amount
            .builder().currency(Currency
                .builder()
                .symbol("ADA")
                .decimals(6)
                .metadata(null)
                .build())
            .value("2000000")
            .build()));

  }

  private static <T> void assertAllElementsIsNull(List<T> g) {
    g.forEach(m -> assertThat(m).isNull());
  }

  @NotNull
  private static Supplier<AssertionError> nullException() {
    return () -> new AssertionError("Not Null");
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
        "poolDeposit");
  }

  private List<Transaction> newTransactions() {
    return List.of(new Transaction("hash1", "blockHash1", 1L, "fee1", 1L,
            true, 1L, null, null,
            newStakeRegistrations(1, 2), newDelegations(1, 2), newPoolRegistrations(1, 2),
            newPoolRetirements(1, 2)),

        new Transaction("hash2", "blockHash2", 2L, "fee2", 2L,
            true, 2L, null, null,
            newStakeRegistrations(3, 4), newDelegations(3, 4), newPoolRegistrations(3, 4),
            newPoolRetirements(3, 4)),

        new Transaction("hash3", "blockHash3", 3L, "fee3", 3L,
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
            .blockHash("blockHash" + ver)
            .build())
        .collect(Collectors.toList());
  }

  private List<PoolRegistration> newPoolRegistrations(int... instances) {
    return Arrays.stream(instances)
        .mapToObj(ver -> PoolRegistration.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .poolId("poolId" + ver)
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
            .poolId("poolId" + ver)
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


