package org.cardanofoundation.rosetta.api.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.OperationStatus;
import org.cardanofoundation.rosetta.api.model.dto.*;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.model.rosetta.BlockMetadata;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.api.common.constants.Constants.*;
import static org.cardanofoundation.rosetta.api.util.Formatters.hexStringFormatter;
import static org.cardanofoundation.rosetta.api.util.RosettaConstants.SUCCESS_OPERATION_STATUS;


@Slf4j
@Component
public class DataMapper {

  /**
   * Maps a NetworkRequest to a NetworkOptionsResponse.
   * @param supportedNetwork The supported network
   * @return The NetworkOptionsResponse
   */
  public static NetworkListResponse mapToNetworkListResponse(Network supportedNetwork) {
    NetworkIdentifier identifier = NetworkIdentifier.builder().blockchain(Constants.CARDANO)
            .network(supportedNetwork.getNetworkId()).build();
    return NetworkListResponse.builder().networkIdentifiers(List.of(identifier)).build();
  }

  /**
   * Maps a NetworkRequest to a NetworkOptionsResponse.
   * @param networkStatus The network status
   * @return The NetworkOptionsResponse
   */
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

  /**
   * Maps a list of AddressBalanceDTOs to a Rosetta compatible AccountBalanceResponse.
   * @param block The block from where the balances are calculated into the past
   * @param poolDeposit The pool deposit
   * @return The Rosetta compatible AccountBalanceResponse
   */
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

  /**
   * Maps a list of TransactionDtos to a list of Rosetta compatible Transactions.
   * @param transactions The transactions to be mapped
   * @param poolDeposit The pool deposit
   * @return The list of Rosetta compatible Transactions
   */
  public static List<Transaction> mapToRosettaTransactions(List<TransactionDto> transactions, String poolDeposit) {
    List<Transaction> rosettaTransactions = new ArrayList<>();
    for(TransactionDto transactionDto : transactions) {
      rosettaTransactions.add(mapToRosettaTransaction(transactionDto, poolDeposit));
    }
    return rosettaTransactions;
  }

  /**
   * Basic mapping if a value is spent or not.
   * @param value value to be mapped
   * @param spent if the value is spent. Will add a "-" in front of the value if spent.
   * @return the mapped value
   */
  public static String mapValue(String value, boolean spent) {
    return spent ? "-" + value : value;
  }

  public static CoinChange getCoinChange(int index, String hash, CoinAction coinAction) {
    CoinIdentifier coinIdentifier = new CoinIdentifier();
    coinIdentifier.setIdentifier(hash + ":" + index);

    return CoinChange.builder().coinIdentifier(CoinIdentifier.builder().identifier(hash + ":" + index).build())
            .coinAction(coinAction.toString()).build();
  }

  /**
   * Maps a TransactionDto to a Rosetta compatible Transaction.
   * @param transactionDto The transaction to be mapped
   * @param poolDeposit The pool deposit
   * @return The Rosetta compatible Transaction
   */
  public static Transaction mapToRosettaTransaction(TransactionDto transactionDto, String poolDeposit) {
    Transaction rosettaTransaction = new Transaction();
    TransactionIdentifier identifier = new TransactionIdentifier();
    identifier.setHash(transactionDto.getHash());
    rosettaTransaction.setTransactionIdentifier(identifier);

    OperationStatus status = new OperationStatus();
//    status.setStatus(Boolean.TRUE.equals(transactionDto.getValidContract()) ? SUCCESS_OPERATION_STATUS.getStatus() : INVALID_OPERATION_STATUS.getStatus());
    status.setStatus(SUCCESS_OPERATION_STATUS.getStatus()); // TODO need to check the right status
    List<Operation> operations = OperationDataMapper.getAllOperations(transactionDto, poolDeposit, status);

    rosettaTransaction.setMetadata(TransactionMetadata.builder()
                    .size(transactionDto.getSize()) // Todo size is not available
                    .scriptSize(transactionDto.getScriptSize()) // TODO script size is not available
            .build());
    rosettaTransaction.setOperations(operations);
    return rosettaTransaction;

  }

  /**
   * Creates a Rosetta compatible Amount for ADA. The value is the amount in lovelace and the currency is ADA.
   * @param value The amount in lovelace
   * @return The Rosetta compatible Amount
   */
  public static Amount mapAmount(String value) {
    if (Objects.isNull(value)) {
      return null;
    }

    Currency currency = Currency.builder()
            .decimals(ADA_DECIMALS)
            .symbol(hexStringFormatter(ADA)).build();
    return Amount.builder().value(value).currency(currency).build();
  }

  /**
   * Creates a Rosetta compatible Amount. Symbol and decimals are optional. If not provided, ADA and 6 decimals are used.
   * @param value The amount of the token
   * @param symbol The symbol of the token - it will be hex encoded
   * @param decimals The number of decimals of the token
   * @param metadata The metadata of the token
   * @return The Rosetta compatible Amount
   */
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
                            .metadata(metadata != null ? new Metadata((String) metadata.get("policyId")) : null) // TODO check metadata for Amount
                            .build());
    return amount;
  }

  /**
   * Maps a list of AddressBalanceDTOs to a Rosetta compatible AccountBalanceResponse.
   * @param block The block from where the balances are calculated into the past
   * @param balances The balances of the addresses
   * @return The Rosetta compatible AccountBalanceResponse
   */
  public static AccountBalanceResponse mapToAccountBalanceResponse(BlockDto block, List<AddressBalanceDTO> balances) {
    List<AddressBalanceDTO> nonLovelaceBalances = balances.stream().filter(balance -> !balance.getAssetName().equals(LOVELACE)).collect(Collectors.toList());
    long sum = balances.stream().filter(balance -> balance.getAssetName().equals(LOVELACE)).mapToLong(value -> value.getQuantity().longValue()).sum();
    List<Amount> amounts = new ArrayList<>();
    if (sum > 0) {
      amounts.add(mapAmount(String.valueOf(sum)));
    }
    nonLovelaceBalances.forEach(balance -> amounts.add(mapAmount(balance.getQuantity().toString(), Hex.encodeHexString(balance.getAssetName().getBytes()), MULTI_ASSET_DECIMALS, Map.of("policyId", balance.getPolicy()))));
    return AccountBalanceResponse.builder()
            .blockIdentifier(BlockIdentifier.builder()
                    .hash(block.getHash())
                    .index(block.getNumber())
                    .build())
            .balances(amounts)
            .build();
  }

  public static AccountBalanceResponse mapToStakeAddressBalanceResponse(BlockDto block, StakeAddressBalanceDTO balance) {
    return AccountBalanceResponse.builder()
            .blockIdentifier(BlockIdentifier.builder()
                    .hash(block.getHash())
                    .index(block.getNumber())
                    .build())
            .balances(List.of(mapAmount(balance.getQuantity().toString())))
            .build();
  }
}





