package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.apache.commons.codec.binary.Hex;
import org.assertj.core.util.introspection.CaseFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.Relay;
import org.openapitools.client.model.TokenBundleItem;
import org.openapitools.client.model.Transaction;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.Amt;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.ADA_DECIMALS;

class BlockTxToRosettaTransactionTest extends BaseMapperTest {


  @Test
  void toDto_Test_empty_operations() {
    //given
    BlockTxToRosettaTransaction my = given();
    BlockTx from = newTran();
    from.setInputs(List.of());
    from.setOutputs(List.of());
    //when
    Transaction into = my.toDto(from, "5000");
    //then
    assertThat(into.getMetadata().getSize()).isEqualTo(from.getSize());
    assertThat(into.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
    assertThat(into.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());
    assertThat(into.getOperations().size()).isEqualTo(0);
  }


  @Test
  void toDto_Test_StakeRegistrationOperations() {
    //given
    BlockTxToRosettaTransaction my = given();
    BlockTx from = newTran();
    List<CertificateType> stakeTypes = List.of(
        CertificateType.STAKE_REGISTRATION,
        CertificateType.STAKE_DEREGISTRATION);

    stakeTypes.forEach(stakeType -> {
      from.setStakeRegistrations(List.of(StakeRegistration.builder()
          .address("stake_addr1")
          .type(stakeType)
          .slot(1L)
          .build()));
      //when
      Transaction into = my.toDto(from, "5000");
      //then
      assertThat(into.getMetadata().getSize()).isEqualTo(from.getSize());
      assertThat(into.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
      assertThat(into.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());

      assertThat(into.getOperations().size()).isEqualTo(3);
      String type = Optional.ofNullable(OperationType.fromValue(convert(stakeType, "stakeKey")))
          .map(OperationType::getValue)
          .orElseThrow(() -> new IllegalArgumentException("Invalid stake type"));
      Optional<Operation> opt = into.getOperations()
          .stream()
          .filter(f -> f.getType().equals(type))
          .findFirst();
      assertThat(opt.isPresent()).isTrue();

      Operation stakeInto = opt.get();
      StakeRegistration firstFrom = from.getStakeRegistrations().getFirst();
      assertThat(stakeInto.getType()).isEqualTo(type);
      assertThat(stakeInto.getStatus()).isEqualTo("success");
      assertThat(stakeInto.getOperationIdentifier().getIndex()).isEqualTo(1); //index in array
      assertThat(stakeInto.getOperationIdentifier().getNetworkIndex()).isNull(); //TODO ??
      assertThat(stakeInto.getAccount().getAddress()).isEqualTo(firstFrom.getAddress());
      assertThat(stakeInto.getMetadata().getDepositAmount())
          .isEqualTo(depositAmountActual("2000000"));
    });

  }


  @Test
  void toDto_Test_DelegationOperations() {
    //given
    BlockTxToRosettaTransaction my = given();
    BlockTx from = newTran();
    CertificateType stakeType = CertificateType.STAKE_DELEGATION;

    from.setDelegations(List.of(Delegation.builder()
        .address("stake_addr1")
        .slot(1L)
        .build()));
    //when
    Transaction into = my.toDto(from, "5000");
    //then
    assertThat(into.getMetadata().getSize()).isEqualTo(from.getSize());
    assertThat(into.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
    assertThat(into.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());

    assertThat(into.getOperations().size()).isEqualTo(3);
    String type = Optional.ofNullable(OperationType.fromValue(convert(stakeType, "stake")))
        .map(OperationType::getValue)
        .orElseThrow(() -> new IllegalArgumentException("Invalid stake type"));
    Optional<Operation> opt = into.getOperations()
        .stream()
        .filter(f -> f.getType().equals(type))
        .findFirst();
    assertThat(opt.isPresent()).isTrue();

    Operation stakeInto = opt.get();
    Delegation firstFrom = from.getDelegations().getFirst();
    assertThat(stakeInto.getType()).isEqualTo(type);
    assertThat(stakeInto.getStatus()).isEqualTo("success");
    assertThat(stakeInto.getOperationIdentifier().getIndex()).isEqualTo(1); //index in array
    assertThat(stakeInto.getAccount().getAddress()).isEqualTo(firstFrom.getAddress());
    assertThat(stakeInto.getMetadata().getPoolKeyHash()).isEqualTo(firstFrom.getPoolId());


  }

  @Test
  void toDto_Test_getPoolRegistrationOperations() {
    //given
    BlockTxToRosettaTransaction my = given();
    BlockTx from = newTran();
    CertificateType poolReg = CertificateType.POOL_REGISTRATION;

    TreeSet<String> owners = new TreeSet<>(); //Need to sort for verification
    owners.add("pool_owner1");
    owners.add("pool_owner2");

    List<Relay> relays = List.of(Relay
        .builder().ipv4("ipv4").ipv6("ipv6")
        .dnsName("dnsName").port(1).type("type").build());

    from.setPoolRegistrations(List.of(PoolRegistration.builder()
        .poolId("pool_addr1")
        .vrfKeyHash("vrf_key_hash1")
        .pledge("pladege1")
        .cost("cost1")
        .margin("margin1")
        .rewardAccount("reward_account1")
        .owners(owners)
        .relays(relays)
        .slot(1L)
        .build()));
    //when
    Transaction into = my.toDto(from, "5000");
    //then
    assertThat(into.getMetadata().getSize()).isEqualTo(from.getSize());
    assertThat(into.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
    assertThat(into.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());

    assertThat(into.getOperations().size()).isEqualTo(3);
    String type = Optional.ofNullable(OperationType.fromValue(convert(poolReg, "pool")))
        .map(OperationType::getValue)
        .orElseThrow(() -> new IllegalArgumentException("Invalid pool type"));
    Optional<Operation> opt = into.getOperations()
        .stream()
        .filter(f -> f.getType().equals(type))
        .findFirst();
    assertThat(opt.isPresent()).isTrue();

    Operation poolInto = opt.get();
    PoolRegistration firstFrom = from.getPoolRegistrations().getFirst();
    assertThat(poolInto.getType()).isEqualTo(type);
    assertThat(poolInto.getStatus()).isEqualTo("success");
    assertThat(poolInto.getOperationIdentifier().getIndex()).isEqualTo(1); //index in array
    assertThat(poolInto.getAccount().getAddress()).isEqualTo(firstFrom.getPoolId());
    assertThat(poolInto.getMetadata().getDepositAmount())
        .isEqualTo(depositAmountActual("5000"));

    PoolRegistrationParams poolRegParams = poolInto.getMetadata().getPoolRegistrationParams();
    assertThat(poolRegParams.getPledge()).isEqualTo(firstFrom.getPledge());
    assertThat(poolRegParams.getCost()).isEqualTo(firstFrom.getCost());
    assertThat(poolRegParams.getPoolOwners()).hasSameElementsAs(firstFrom.getOwners());
    assertThat(poolRegParams.getMarginPercentage()).isEqualTo(firstFrom.getMargin());
    assertThat(poolRegParams.getRelays()).hasSameElementsAs(firstFrom.getRelays());

  }


  @Test
  void toDto_Test_getPoolRetirementOperations() {
    //given
    BlockTxToRosettaTransaction my = given();
    BlockTx from = newTran();
    CertificateType poolReg = CertificateType.POOL_RETIREMENT;

    from.setPoolRetirements(List.of(PoolRetirement.builder()
        .poolId("pool_addr1")
        .slot(1L)
        .epoch(11)
        .certIndex(33)
        .blockHash("blockHash1")
        .txHash("txHash1")
        .build()));

    //when
    Transaction into = my.toDto(from, "5000");
    //then
    assertThat(into.getMetadata().getSize()).isEqualTo(from.getSize());
    assertThat(into.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
    assertThat(into.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());

    assertThat(into.getOperations().size()).isEqualTo(3);
    String type = Optional.ofNullable(OperationType.fromValue(convert(poolReg, "pool")))
        .map(OperationType::getValue)
        .orElseThrow(() -> new IllegalArgumentException("Invalid pool type"));
    Optional<Operation> opt = into.getOperations()
        .stream()
        .filter(f -> f.getType().equals(type))
        .findFirst();
    assertThat(opt.isPresent()).isTrue();

    Operation poolInto = opt.get();
    PoolRetirement firstFrom = from.getPoolRetirements().getFirst();
    assertThat(poolInto.getType()).isEqualTo(type);
    assertThat(poolInto.getStatus()).isEqualTo("success");
    assertThat(poolInto.getOperationIdentifier().getIndex()).isEqualTo(1); //index in array
    assertThat(poolInto.getAccount().getAddress()).isEqualTo(firstFrom.getPoolId());
    assertThat(poolInto.getMetadata().getEpoch()).isEqualTo(firstFrom.getEpoch());


  }

  @Test
  void toDto_Test_getOutputsAsOperations() {
    //given
    BlockTxToRosettaTransaction my = given();
    BlockTx from = newTran();
    //when
    Transaction into = my.toDto(from, "5000");
    //then
    assertThat(into.getMetadata().getSize()).isEqualTo(from.getSize());
    assertThat(into.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
    assertThat(into.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());
    assertThat(into.getOperations().size()).isEqualTo(2);

    Optional<Operation> opt = into.getOperations()
        .stream()
        .filter(f -> f.getType().equals(Constants.OUTPUT))
        .findFirst();
    assertThat(opt.isPresent()).isTrue();

    Operation opInto = opt.get();
    Utxo firstFrom = from.getOutputs().getFirst();
    assertThat(opInto.getType()).isEqualTo(Constants.OUTPUT);
    assertThat(opInto.getStatus()).isEqualTo("success");
    assertThat(opInto.getOperationIdentifier().getIndex()).isEqualTo(1); //index in array
    assertThat(opInto.getAccount().getAddress()).isEqualTo(firstFrom.getOwnerAddr());
    assertThat(opInto.getAmount()).isEqualTo(depositAmountActual("10"));

    CoinChange coinChange = opInto.getCoinChange();
    assertThat(coinChange.getCoinAction()).isEqualTo(CoinAction.CREATED);
    assertThat(coinChange.getCoinIdentifier().getIdentifier())
        .isEqualTo( firstFrom.getTxHash() + ":" + firstFrom.getOutputIndex() );

    assertThat(opInto.getMetadata().getTokenBundle().size()).isEqualTo(1);
    TokenBundleItem bundle = opInto.getMetadata().getTokenBundle().getFirst();

    assertThat(bundle.getPolicyId())
        .isEqualTo(from.getOutputs().getFirst().getAmounts().getFirst().getPolicyId());
    assertThat(bundle.getTokens().size()).isEqualTo(1);

    Amount token = bundle.getTokens().getFirst();
    assertThat(token.getValue())
        .isEqualTo(from.getOutputs().getFirst().getAmounts().getFirst().getQuantity().toString());
    assertThat(token.getCurrency().getSymbol())
        .isEqualTo(Hex.encodeHexString(
            from.getOutputs().getFirst().getAmounts().getFirst().getAssetName().getBytes()));
    assertThat(token.getCurrency().getDecimals()).isEqualTo(0);

  }

  private static Amount depositAmountActual(String value) {
    return Amount.builder()
        .currency(Currency.builder().symbol(ADA).decimals(ADA_DECIMALS).build())
        .value(value) //TODO should be checked from genesis json settings?
        .build();
  }

  @NotNull
  private BlockTxToRosettaTransaction given() {
    BlockTxToRosettaTransaction my = new BlockTxToRosettaTransaction(modelMapper);
    my.modelMapper.validate();
    return my;
  }

  private static String convert(CertificateType stakeType, String prefix) {
    String name = stakeType.name();
    int idx = name.lastIndexOf("_") + 1;

    return CaseFormatUtils.toCamelCase(prefix + " " + name.substring(idx));
  }

  private BlockTx newTran() {
    return BlockTx
        .builder()
        .blockNo(11L)
        .blockHash("blockHash11")
        .size(1L)
        .fee("123")
        .hash("hash12")
        .scriptSize(0L)
        .inputs(List.of(newUtxoIn()))
        .outputs(List.of(newUtxoOut()))
        .validContract(true)
        .build();
  }

  private Utxo newUtxoIn() {
    return Utxo.builder()
        .blockHash("in_blockHash1")
        .epoch(11)
        .slot(22L)
        .txHash("txHash1")
        .outputIndex(44)
        .amounts(List.of(newAmt()))
        .dataHash("in_dataHash1")
        .inlineDatum("in_inlineDatum1")
        .isCollateralReturn(true)
        .lovelaceAmount(BigInteger.TEN)
        .ownerAddr("in_ownerAddr1")
        .ownerAddrFull("ownerAddrFull1")
        .ownerPaymentCredential("in_ownerPaymentCredential1")
        .ownerStakeAddr("in_ownerStakeAddr1")
        .scriptRef("in_scriptRef1")
        .referenceScriptHash("in_referenceScriptHash1")
        .build();
  }

  private Utxo newUtxoOut() {
    return Utxo.builder()
        .blockHash("in_blockHash1")
        .epoch(11)
        .slot(22L)
        .txHash("txHash1")
        .outputIndex(44)
        .amounts(List.of(newAmt()))
        .dataHash("out_dataHash1")
        .inlineDatum("out_inlineDatum1")
        .isCollateralReturn(true)
        .lovelaceAmount(BigInteger.TEN)
        .ownerAddr("out_ownerAddr1")
        .ownerAddrFull("ownerAddrFull1")
        .ownerPaymentCredential("out_ownerPaymentCredential1")
        .ownerStakeAddr("out_ownerStakeAddr1")
        .scriptRef("out_scriptRef1")
        .referenceScriptHash("out_referenceScriptHash1")
        .build();
  }

  private static Amt newAmt() {
    return Amt.builder()
        .assetName("assetName1")
        .policyId("policyId1")
        .quantity(BigInteger.ONE)
        .unit("unit1")
        .build();
  }


}