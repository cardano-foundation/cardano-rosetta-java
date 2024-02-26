package org.cardanofoundation.rosetta.api.mapper;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.OperationStatus;
import org.cardanofoundation.rosetta.api.model.dto.*;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.model.rosetta.BlockMetadata;
import org.cardanofoundation.rosetta.common.model.TxnEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.api.common.constants.Constants.*;
import static org.cardanofoundation.rosetta.api.util.Formatters.hexStringFormatter;
import static org.cardanofoundation.rosetta.api.util.RosettaConstants.SUCCESS_OPERATION_STATUS;


@Slf4j
@Component
public class DataMapper {

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
      rosettaBlock.setTransactions(mapToRosettaTransactions(block.getTransactions(), poolDeposit));
      rosettaBlock.metadata(BlockMetadata.builder()
              .transactionsCount(block.getTransactionsCount())
              .createdBy(block.getCreatedBy())
              .size(block.getSize())
              .epochNo(block.getEpochNo())
              .slotNo(block.getSlotNo())
              .build());
      return rosettaBlock;
    }

  public static List<Transaction> mapToRosettaTransactions(List<TransactionDto> transactions, String poolDeposit) {
    List<Transaction> rosettaTransactions = new ArrayList<>();
    for(TransactionDto transactionDto : transactions) {
      rosettaTransactions.add(mapToRosettaTransaction(transactionDto, poolDeposit));
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

  public static Transaction mapToRosettaTransaction(TransactionDto transactionDto, String poolDeposit) {
    Transaction rosettaTransaction = new Transaction();
    TransactionIdentifier identifier = new TransactionIdentifier();
    identifier.setHash(transactionDto.getHash());
    rosettaTransaction.setTransactionIdentifier(identifier);

    OperationStatus status = new OperationStatus();
//    status.setStatus(Boolean.TRUE.equals(transactionDto.getValidContract()) ? SUCCESS_OPERATION_STATUS.getStatus() : INVALID_OPERATION_STATUS.getStatus());
    status.setStatus(SUCCESS_OPERATION_STATUS.getStatus()); // TODO need to check the right status
    List<List<Operation>> totalOperations = new ArrayList<>();
    List<Operation> inputsAsOperations = OperationDataMapper.getInputTransactionsasOperations(transactionDto, status);
    totalOperations.add(inputsAsOperations);

    List<Operation> stakeRegistrationOperations = OperationDataMapper.getStakeRegistrationOperations(transactionDto, status, totalOperations);
    totalOperations.add(stakeRegistrationOperations);

    List<Operation> delegationOperations = OperationDataMapper.getDelegationOperations(transactionDto, status, totalOperations);
    totalOperations.add(delegationOperations);

    List<Operation> poolRegistrationOperations = OperationDataMapper.getPoolRegistrationOperations(transactionDto, status, totalOperations, poolDeposit);
    totalOperations.add(poolRegistrationOperations);

    List<Operation> poolRetirementOperations = OperationDataMapper.getPoolRetirementOperations(transactionDto, status, totalOperations);
    totalOperations.add(poolRetirementOperations);

    List<OperationIdentifier> relatedOperations = OperationDataMapper.getOperationIndexes(inputsAsOperations);

    List<Operation> outputsAsOperations = OperationDataMapper.getOutputsAsOperations(transactionDto, totalOperations, status, relatedOperations);

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

  public static Amount mapAmount(String value) {
    if (Objects.isNull(value)) {
      return null;
    }

    Currency currency = Currency.builder()
            .decimals(ADA_DECIMALS)
            .symbol(hexStringFormatter(ADA)).build();
    return Amount.builder().value(value).currency(currency).build();
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
    amount.setValue(value);
    amount.setCurrency(Currency.builder()
                            .symbol(hexStringFormatter(symbol))
                            .decimals(decimals)
//                            .metadata() // TODO check metadata for Amount
                            .build());
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
//                                    .metadata(addressBalanceDTO.getPolicy() != null ? Map.of("policyId", addressBalanceDTO.getPolicy()) : null) // TODO check metadata for AccountBalanceResponse
                                    .build()).build()).toList())
            .build();
  }
}





