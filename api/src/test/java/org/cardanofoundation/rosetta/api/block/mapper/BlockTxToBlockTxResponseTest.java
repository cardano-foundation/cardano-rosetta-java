package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.openapitools.client.model.BlockTransactionResponse;
import org.openapitools.client.model.Relay;
import org.openapitools.client.model.Transaction;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;

import static org.assertj.core.api.Assertions.assertThat;

class BlockTxToBlockTxResponseTest extends BaseMapperTest {

  @Test
  void toDto() {
    //given
    BlockTxToRosettaTransaction txMapper = new BlockTxToRosettaTransaction(modelMapper);
    BlockTxToBlockTxResponse my = new BlockTxToBlockTxResponse(modelMapper, txMapper);
    BlockTx from = newTran();
    // when
    BlockTransactionResponse into = my.toDto(from, "5000");
    // then
    my.modelMapper.validate();

    Transaction tx = into.getTransaction();
    assertThat(tx.getMetadata().getSize()).isEqualTo(from.getSize());
    assertThat(tx.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
    assertThat(tx.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());

    //TODO saa: there are no related transactions for org.openapitools.client.model.Transaction.setRelatedTransactions
    assertThat(tx.getRelatedTransactions()).isNull();
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
        .validContract(true)
        .inputs(List.of(newUtxo()))
        .delegations(newDelegations())
        .poolRegistrations(newPoolRegistrations())
        .poolRetirements(newPoolRetirements())
        .stakeRegistrations(newStakeRegistrations())
        .build();
  }
  private Utxo newUtxo() {
    return Utxo.builder()
        .blockHash("blockHash1")
        .epoch(11)
        .slot(22L)
        .txHash("txHash1")
        .outputIndex(44)
        .amounts(List.of(newAmt()))
        .dataHash("dataHash1")
        .inlineDatum("inlineDatum1")
        .isCollateralReturn(true)
        .lovelaceAmount(BigInteger.TEN)
        .ownerAddr("ownerAddr1")
        .ownerAddrFull("ownerAddrFull1")
        .ownerPaymentCredential("ownerPaymentCredential1")
        .ownerStakeAddr("ownerStakeAddr1")
        .scriptRef("scriptRef1")
        .referenceScriptHash("referenceScriptHash1")
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
  private List<StakeRegistration> newStakeRegistrations() {
    return List.of(StakeRegistration.builder()
        .blockHash("stakeReg_blockHash1")
        .epoch(11)
        .type(CertificateType.STAKE_DEREGISTRATION)
        .slot(22L)
        .certIndex(33L)
        .address("stakeReg_address1")
        .credential("stakeReg_credential1")
        .build());
  }

  private List<PoolRetirement> newPoolRetirements() {
    return List.of(PoolRetirement.builder()
        .blockHash("poolRet_blockHash1")
        .epoch(11)
        .slot(22L)
        .certIndex(33L)
        .poolId("poolRet_poolId1")
        .txHash("poolRet_txHash1")
        .build());
  }

  private List<PoolRegistration> newPoolRegistrations() {
    return List.of(PoolRegistration.builder()
        .blockHash("poolReg_blockHash1")
        .epoch(11)
        .slot(22L)
        .certIndex(33L)
        .poolId("poolReg_poolId1")
        .vrfKeyHash("poolReg_vrfKey1")
        .pledge("poolReg_pledge1")
        .margin("poolReg_margin1")
        .cost("poolReg_cost1")
        .rewardAccount("poolReg_rewardAccount1")
        .owners(Set.of("poolReg_owner1"))
        .relays(List.of(Relay.builder().ipv4("poolReg_ipv4_1").
            ipv6("poolReg_ipv6_1").port(123).dnsName("dnsName1").build()))
        .build());
  }

  private List<Delegation> newDelegations() {
    return List.of(Delegation.builder()
        .blockHash("delegation_blockHash1")
        .epoch(11)
        .slot(22L)
        .certIndex(33L)
        .poolId("delegation_poolId1")
        .credential("delegation_account1")
        .txHash("delegation_txHash1")
        .address("delegation_address1")
        .build());
  }

}
