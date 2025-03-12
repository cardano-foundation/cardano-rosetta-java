package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.openapitools.client.model.BlockTransactionResponse;
import org.openapitools.client.model.Relay;
import org.openapitools.client.model.Transaction;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.StakePoolDelegation;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;

import static org.assertj.core.api.Assertions.assertThat;

class BlockTxToBlockTxResponseTest extends BaseMapperSetup {

  @Autowired
  private BlockMapper my;

  @Test
  void mapToBlockTransactionResponse() {
    //given
    BlockTx from = newTran();
    // when
    BlockTransactionResponse into = my.mapToBlockTransactionResponse(from);
    // then

    Transaction tx = into.getTransaction();
    assertThat(tx.getMetadata().getSize()).isEqualTo(from.getSize());
    assertThat(tx.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
    assertThat(tx.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());

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
        .inputs(List.of(newUtxo()))
        .stakePoolDelegations(newDelegations())
        .poolRegistrations(newPoolRegistrations())
        .poolRetirements(newPoolRetirements())
        .stakeRegistrations(newStakeRegistrations())
        .build();
  }

  private Utxo newUtxo() {
    return Utxo.builder()
        .txHash("txHash1")
        .outputIndex(44)
        .amounts(List.of(newAmt()))
        .ownerAddr("ownerAddr1")
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
        .type(CertificateType.STAKE_DEREGISTRATION)
        .certIndex(33L)
        .address("stakeReg_address1")
        .build());
  }

  private List<PoolRetirement> newPoolRetirements() {
    return List.of(PoolRetirement.builder()
        .epoch(11)
        .poolId("poolRet_poolId1")
        .txHash("poolRet_txHash1")
        .build());
  }

  private List<PoolRegistration> newPoolRegistrations() {
    return List.of(PoolRegistration.builder()
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

  private List<StakePoolDelegation> newDelegations() {
    return List.of(StakePoolDelegation.builder()
        .certIndex(33L)
        .poolId("delegation_poolId1")
        .txHash("delegation_txHash1")
        .address("delegation_address1")
        .build());
  }

}
