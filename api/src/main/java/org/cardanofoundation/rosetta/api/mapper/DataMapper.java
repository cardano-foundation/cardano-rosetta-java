package org.cardanofoundation.rosetta.api.mapper;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.model.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.model.dto.UtxoDto;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.common.model.TxnEntity;
import org.openapitools.client.model.Block;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component

public class DataMapper {

  public static final String COIN_SPENT_ACTION = "coin_spent";
  public static final String COIN_CREATED_ACTION = "coin_created";

  private DataMapper() {

  }

  public static NetworkListResponse mapToNetworkListResponse(Network supportedNetwork) {
    NetworkIdentifier identifier = NetworkIdentifier.builder().blockchain(Constants.CARDANO)
            .network(supportedNetwork.getNetworkId()).build();
    return NetworkListResponse.builder().networkIdentifiers(List.of(identifier)).build();
  }

  public static NetworkStatusResponse mapToNetworkStatusResponse(NetworkStatus networkStatus) {
    BlockDto latestBlock = networkStatus.getLatestBlock();
    GenesisBlockDto genesisBlock = networkStatus.getGenesisBlock();
    List<Peer> peers = networkStatus.getPeers();
    return NetworkStatusResponse.builder()
            .currentBlockIdentifier(
                    BlockIdentifier.builder().index(latestBlock.getNumber()).hash(latestBlock.getHash())
                            .build())
            .currentBlockTimeStamp(latestBlock.getCreatedAt())
            .genesisBlockIdentifier(BlockIdentifier.builder().index(
                            genesisBlock.getNumber() != null ? genesisBlock.getNumber() : 0)
                    .hash(genesisBlock.getHash()).build())
            .peers(peers.stream().map(peer -> new Peer(peer.getAddr())).toList())
            .build();
  }

  public static List<TransactionDto> parseTransactionRows(
          List<TxnEntity> findTransactions) {
    return findTransactions.stream()
            .map(tx -> TransactionDto.builder()
                    .hash(tx.getTxHash())
                    .blockHash(
                            tx.getBlock().getHash())
                    .blockNo(tx.getBlock().getNumber())
                    .fee(String.valueOf(tx.getFee()))
                    .inputs(tx.getInputs().stream().map(utxoKey -> new UtxoDto(utxoKey.getTxHash(), utxoKey.getOutputIndex())).toList())
                    .outputs(tx.getOutputs().stream().map(utxoKey -> new UtxoDto(utxoKey.getTxHash(), utxoKey.getOutputIndex())).toList())
//                    .size(Long.valueOf(tx.get)) // TODO
//                    .scriptSize(Long.valueOf(tx.getScriptSize())) // TODO
//                    .validContract(tx.) // TODO
                    .build())
            .collect(Collectors.toList());
  }



    public static Block mapToRosettaBlock(BlockDto block, String poolDeposit) {
      Block rosettaBlock = new Block();
      rosettaBlock.setBlockIdentifier(new org.openapitools.client.model.BlockIdentifier()
              .index(block.getNumber()).hash(block.getHash()));
      rosettaBlock.setParentBlockIdentifier(new org.openapitools.client.model.BlockIdentifier()
              .index(block.getPreviousBlockNumber()).hash(block.getPreviousBlockHash()));
      rosettaBlock.setTimestamp(block.getCreatedAt());
      rosettaBlock.setTransactions(mapToRosettaTransactions(block.getTransactions()));
//      rosettaBlock.metadata() // TODO

      return rosettaBlock;
    }

  public static List<org.openapitools.client.model.Transaction> mapToRosettaTransactions(List<TransactionDto> transactions) {
    List<org.openapitools.client.model.Transaction> rosettaTransactions = new ArrayList<>();
    for(TransactionDto transactionDto : transactions) {

      rosettaTransactions.add(mapToRosettaTransaction(transactionDto));
    }
    return rosettaTransactions;
  }

  public static org.openapitools.client.model.Transaction mapToRosettaTransaction(TransactionDto transactionDto) {
    org.openapitools.client.model.Transaction rosettaTransaction = new org.openapitools.client.model.Transaction();
    org.openapitools.client.model.TransactionIdentifier identifier = new org.openapitools.client.model.TransactionIdentifier();
    identifier.setHash(transactionDto.getHash());

    rosettaTransaction.setTransactionIdentifier(identifier);
//      rosettaTransaction.setRelatedTransactions(); // TODO
    List<org.openapitools.client.model.Operation> operation = new ArrayList<>();
//      TODO
    rosettaTransaction.setOperations(operation);
//      rosettaTransaction.setMetadata(); // TODO
    return rosettaTransaction;

  }

}





