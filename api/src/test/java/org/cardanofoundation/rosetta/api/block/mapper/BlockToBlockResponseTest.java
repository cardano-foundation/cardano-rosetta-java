package org.cardanofoundation.rosetta.api.block.mapper;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import jakarta.validation.constraints.NotNull;
import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.*;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;
import org.openapitools.client.model.CurrencyResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

class BlockToBlockResponseTest extends BaseMapperSetup {


  @Autowired
  private BlockMapper my;

  @Test
  void mapToBlockResponse_test_invalidTransaction() {
    String policyId = "tAda";
    String symbol = "123";
    String unit = policyId + symbol;

    BlockTx build = BlockTx.builder()
        .invalid(true)
        .blockHash("Hash")
        .blockNo(1L)
        .inputs(
            List.of(Utxo.builder().txHash("Hash").outputIndex(0).ownerAddr("Owner").amounts(List.of(
                    Amt.builder().unit(unit).policyId(policyId).quantity(BigInteger.valueOf(10L)).build()))
                .build()))
        .outputs(
            List.of(Utxo.builder().txHash("Hash").outputIndex(0).ownerAddr("Owner").amounts(List.of(
                    Amt.builder().unit(unit).policyId(policyId).quantity(BigInteger.valueOf(10L)).build()))
                .build()))
        .build();

    // Create test metadata map for the asset
    Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap = Map.of(
        AssetFingerprint.of(policyId, symbol),
        TokenRegistryCurrencyData.builder().decimals(6).build()
    );

    BlockTransactionResponse blockTransactionResponse = my.mapToBlockTransactionResponseWithMetadata(build, metadataMap);
    // Since the Transaction is invalid we are not returning any outputs. The outputs will be removed, since there aren't any outputs
    assertThat(blockTransactionResponse.getTransaction().getOperations()).hasSize(1);
  }

  @Test
  void mapToBlockResponse_test_Ok() {

    //given
    Block from = newBlock();

    // Create empty metadata map since this test uses only stake operations, no native tokens
    Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap = Map.of();

    //when
    BlockResponse into = my.mapToBlockResponseWithMetadata(newBlock(), metadataMap);

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
        .toList();
    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getStatus)
            .toList())
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
            .toList())
        .containsAnyOf(opIds);

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getRelatedOperations)
            .toList())
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
            .toList())
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
            .toList())
        .containsAnyOf(accIds);

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .map(Operation::getCoinChange)
            .toList())
        .allSatisfy(BlockToBlockResponseTest::assertAllElementsIsNull);

    CurrencyResponse ada = CurrencyResponse
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
            .toList())
        .allSatisfy(d -> {
          assertAllPropertiesIsNull(d, "depositAmount");
          assertProperty(d, "depositAmount",
              Amount
                  .builder()
                  .currency(ada)
                  .value("0")
                  .build());
        });

    AtomicInteger aiPool = new AtomicInteger(0); //just immutable helper

    assertThat((into.getBlock().getTransactions()))
        .extracting(t -> t.getOperations()
            .stream()
            .filter(
                g -> g.getType().equals("poolRegistration"))
            .map(Operation::getMetadata)
            .toList())
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
            .toList())
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
            .toList())
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
            BlockTx.builder()
                    .hash("hash1")
                    .blockHash("   blockHash1")
                    .blockNo(1L)
                    .fee("fee1")
                    .size(1L)
                    .scriptSize(1L)
                    .invalid(false)
                    .stakeRegistrations(newStakeRegistrations(1, 2))
                    .stakePoolDelegations(newDelegations(1, 2))
                    .poolRegistrations(newPoolRegistrations(1, 2))
                    .poolRetirements(newPoolRetirements(1, 2))
                    .governancePoolVotes(List.of()) // TODO governance votes (from db)
                    .dRepDelegations(List.of()) // TODO drep vote delegation (from db)
                    .build(),
            BlockTx.builder()
                    .hash("hash2")
                    .blockHash("blockHash2")
                    .blockNo(2L)
                    .fee("fee2")
                    .size(2L)
                    .scriptSize(2L)
                    .invalid(false)
                    .stakeRegistrations(newStakeRegistrations(3, 4))
                    .stakePoolDelegations(newDelegations(3, 4))
                    .poolRegistrations(newPoolRegistrations(3, 4))
                    .poolRetirements(newPoolRetirements(3, 4))
                    .dRepDelegations(List.of()) // TODO drep vote delegation (from db)
                    .governancePoolVotes(List.of()) // TODO governance votes (from db)
                    .build(),
        BlockTx.builder()
                    .hash("hash3")
                    .blockHash("blockHash3")
                    .blockNo(3L)
                    .fee("fee3")
                    .size(3L)
                    .scriptSize(3L)
                    .invalid(false)
                    .stakeRegistrations(newStakeRegistrations(5, 6))
                    .stakePoolDelegations(newDelegations(5, 6))
                    .poolRegistrations(newPoolRegistrations(5, 6))
                    .poolRetirements(newPoolRetirements(5, 6))
                    .dRepDelegations(List.of()) // TODO drep vote delegation (from db)
                    .governancePoolVotes(List.of()) // TODO governance votes (from db)
                    .build()
    );
  }

  private List<StakePoolDelegation> newDelegations(int... instances) {
    return Arrays.stream(instances)
        .mapToObj(ver -> StakePoolDelegation.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .poolId("poolDlg" + ver)
            .address("delegationAcc" + ver)
            .build())
        .toList();
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
        .toList();
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
        .toList();
  }

  private List<StakeRegistration> newStakeRegistrations(int... instances) {

    return Arrays.stream(instances)
        .mapToObj(ver -> StakeRegistration.builder()
            .txHash("txHash" + ver)
            .certIndex(1L + ver)
            .type(CertificateType.STAKE_REGISTRATION)
            .address("address" + ver)
            .build())
        .toList();


  }
}
