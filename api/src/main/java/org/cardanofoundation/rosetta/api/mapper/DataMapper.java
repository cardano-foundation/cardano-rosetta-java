package org.cardanofoundation.rosetta.api.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.tomcat.util.bcel.Const;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.OperationStatus;
import org.cardanofoundation.rosetta.api.model.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.dto.AddressBalanceDTO;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.model.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.model.dto.UtxoDto;
import org.cardanofoundation.rosetta.api.model.rest.*;

import org.cardanofoundation.rosetta.api.model.rosetta.BlockMetadata;
import org.cardanofoundation.rosetta.common.model.Amt;
import org.cardanofoundation.rosetta.common.model.TxnEntity;

import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;
import static org.cardanofoundation.rosetta.api.common.constants.Constants.*;
import static org.cardanofoundation.rosetta.api.util.Formatters.hexStringFormatter;
import static org.cardanofoundation.rosetta.api.util.RosettaConstants.INVALID_OPERATION_STATUS;
import static org.cardanofoundation.rosetta.api.util.RosettaConstants.SUCCESS_OPERATION_STATUS;


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
                    .inputs(tx.getInputKeys().stream().map(utxoKey -> new UtxoDto(utxoKey.getTxHash(), utxoKey.getOutputIndex())).toList())
                    .outputs(tx.getOutputKeys().stream().map(utxoKey -> new UtxoDto(utxoKey.getTxHash(), utxoKey.getOutputIndex())).toList())
//                    .size(Long.valueOf(tx.get)) // TODO
//                    .scriptSize(Long.valueOf(tx.getScriptSize())) // TODO
//                    .validContract(tx.) // TODO
                    .build())
            .collect(Collectors.toList());
  }



    public static Block mapToRosettaBlock(BlockDto block, String poolDeposit) {
      Block rosettaBlock = Block.builder().build();
      rosettaBlock.setBlockIdentifier(BlockIdentifier.builder()
              .index(block.getNumber()).hash(block.getHash()).build());
      rosettaBlock.setParentBlockIdentifier(BlockIdentifier.builder()
              .index(block.getPreviousBlockNumber()).hash(block.getPreviousBlockHash()).build());
      rosettaBlock.setTimestamp(block.getCreatedAt());
      rosettaBlock.setTransactions(mapToRosettaTransactions(block.getTransactions()));
      rosettaBlock.metadata(BlockMetadata.builder()
              .transactionsCount(block.getTransactionsCount())
              .createdBy(block.getCreatedBy())
              .size(block.getSize())
              .epochNo(block.getEpochNo())
              .slotNo(block.getSlotNo())
              .build());
      return rosettaBlock;
    }

  public static List<Transaction> mapToRosettaTransactions(List<TransactionDto> transactions) {
    List<Transaction> rosettaTransactions = new ArrayList<>();
    for(TransactionDto transactionDto : transactions) {

      rosettaTransactions.add(mapToRosettaTransaction(transactionDto));
    }
    return rosettaTransactions;
  }

  public static String mapValue(String value, boolean spent) {
    return spent ? "-" + value : value;
  }

  public static CoinChange getCoinChange(int index, String hash, CoinAction coinAction) {
    CoinIdentifier coinIdentifier = new CoinIdentifier();
    coinIdentifier.setIdentifier(hash + ":" + index);

    return CoinChange.builder().coinIdentifier(CoinIdentifier.builder().identifier(hash + ":" + index).build())
            .coinAction(coinAction.toString()).build();
  }

  private static OperationMetadata mapToOperationMetaData(boolean spent, List<Amt> amounts) {
    OperationMetadata operationMetadata = new OperationMetadata();
    for (Amt amount : amounts) {
      if(!amount.getAssetName().equals(Constants.LOVELACE)) {
        TokenBundleItem tokenBundleItem = new TokenBundleItem();
        tokenBundleItem.setPolicyId(amount.getPolicyId());
        Amount amt = new Amount();
        amt.setValue(mapValue(amount.getQuantity().toString(), spent));
        String hexAssetName = Hex.encodeHexString(amount.getAssetName().getBytes());
        amt.setCurrency(Currency.builder()
                .symbol(hexStringFormatter(hexAssetName))
                .decimals(0)
                .build());
        tokenBundleItem.setTokens(List.of(amt));
        operationMetadata.addTokenBundleItem(tokenBundleItem);
      }
    }


    return operationMetadata;
  }

  public static Transaction mapToRosettaTransaction(TransactionDto transactionDto) {
    Transaction rosettaTransaction = new Transaction();
    TransactionIdentifier identifier = new TransactionIdentifier();
    identifier.setHash(transactionDto.getHash());
    rosettaTransaction.setTransactionIdentifier(identifier);

    OperationStatus status = new OperationStatus();
//    status.setStatus(Boolean.TRUE.equals(transactionDto.getValidContract()) ? SUCCESS_OPERATION_STATUS.getStatus() : INVALID_OPERATION_STATUS.getStatus());
    status.setStatus(SUCCESS_OPERATION_STATUS.getStatus()); // TODO need to check the right status
    List<List<Operation>> totalOperations = new ArrayList<>();
    List<Operation> inputsAsOperations = IntStream.range(0, transactionDto.getInputs().size())
            .mapToObj(index -> {
              UtxoDto utxoDto = transactionDto.getInputs().get(index);
              return createOperation((long) index,
                      Constants.INPUT,
                      status.getStatus(),
                      utxoDto.getOwnerAddr(),
                      mapValue(utxoDto.getLovelaceAmount().toString(), true),
                      null,
                      null,
                      getCoinChange(utxoDto.getOutputIndex(), utxoDto.getTxHash(),
                              CoinAction.SPENT),
                      mapToOperationMetaData(true, utxoDto.getAmounts()));
            }).toList();

    List<OperationIdentifier> relatedOperations = getOperationIndexes(inputsAsOperations);

    List<Operation> outputsAsOperations = IntStream.range(0, transactionDto.getOutputs().size())
                    .mapToObj(index -> {
                      UtxoDto utxoDto = transactionDto.getOutputs().get(index);
                      return createOperation(getOperationCurrentIndex(totalOperations, index),
                              Constants.OUTPUT,
                              status.getStatus(),
                              utxoDto.getOwnerAddr(),
                              mapValue(utxoDto.getLovelaceAmount().toString(), false),
                              relatedOperations,
                              Long.valueOf(utxoDto.getOutputIndex()),
                              getCoinChange(utxoDto.getOutputIndex(), utxoDto.getTxHash(),
                                      CoinAction.CREATED),
                              mapToOperationMetaData(false, utxoDto.getAmounts()));
                    }).toList();
    totalOperations.add(inputsAsOperations);
    totalOperations.add(outputsAsOperations);
    List<Operation> operations = totalOperations.stream()
            .flatMap(java.util.Collection::stream)
            .collect(Collectors.toList());

    rosettaTransaction.setMetadata(TransactionMetadata.builder()
                    .size(transactionDto.getSize()) // Todo size is not available
                    .scriptSize(transactionDto.getScriptSize()) // TODO script size is not available
            .build());
    rosettaTransaction.setOperations(operations);
    return rosettaTransaction;

  }

  public static long getOperationCurrentIndex(List<List<Operation>> operationsList,
                                              int relativeIndex) {
    return relativeIndex + operationsList.stream()
            .mapToLong(List::size)
            .sum();
  }

  public static List<OperationIdentifier> getOperationIndexes(List<Operation> operations) {
    return operations.stream()
            .map(operation -> OperationIdentifier.builder().index(operation.getOperationIdentifier()
                    .getIndex()).build()).collect(Collectors.toList());
  }

  public static Operation createOperation(Long index, String type, String status, String address,
                                          String value, List<OperationIdentifier> relatedOperations, Long networkIndex,
                                          CoinChange coinChange, OperationMetadata tokenBundleMetadata) {
    return Operation.builder()
            .operationIdentifier(OperationIdentifier.builder()
                    .index(index)
                    .networkIndex(networkIndex).build()
            )
            .type(type)
            .status(status)
            .account(AccountIdentifier.builder()
                    .address(address).build()
            )
            .amount(mapAmount(value, null, null, null))
            .coinChange(coinChange)
            .relatedOperations(relatedOperations)
            .metadata(tokenBundleMetadata).build();

  }

  public static Amount mapAmount(String value, String symbol, Integer decimals,
                                 Map<String, Object> metadata) {
    if (Objects.isNull(symbol)) {
      symbol = ADA;
    }
    if (Objects.isNull(decimals)) {
      decimals = ADA_DECIMALS;
    }
    Amount amount = new Amount();
    Currency currency = new Currency();
    amount.setValue(value);
    amount.setCurrency(Currency.builder()
                            .symbol(hexStringFormatter(symbol))
                            .decimals(decimals)
//                            .metadata(metadata) // TODO
                    .build()
            );
    return amount;
  }

  public static AccountBalanceResponse mapToAccountBalanceResponse(BlockDto block, List<AddressBalanceDTO> balances) {
    return AccountBalanceResponse.builder()
            .blockIdentifier(BlockIdentifier.builder()
                    .hash(block.getHash())
                    .index(block.getNumber())
                    .build())
            .balances(balances.stream().map(addressBalanceDTO -> Amount.builder()
                            .value(addressBalanceDTO.getQuantity().toString())
                            .currency(Currency.builder()
                                    .decimals(MULTI_ASSET_DECIMALS)
                                    .symbol(addressBalanceDTO.getUnit())
//                                    .metadata(addressBalanceDTO.getPolicy() != null ? Map.of("policyId", addressBalanceDTO.getPolicy()) : null)
                                    .build()).build()).toList())
            .build();
  }
}





