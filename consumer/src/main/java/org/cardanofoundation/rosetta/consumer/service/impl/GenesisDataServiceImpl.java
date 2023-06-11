package org.cardanofoundation.rosetta.consumer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.*;
import org.cardanofoundation.rosetta.consumer.dto.GenesisData;
import org.cardanofoundation.rosetta.consumer.repository.*;
import org.cardanofoundation.rosetta.consumer.service.CostModelService;
import org.cardanofoundation.rosetta.consumer.service.EpochParamService;
import org.cardanofoundation.rosetta.consumer.service.GenesisDataService;
import org.cardanofoundation.rosetta.consumer.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Profile("!test-integration")
public class GenesisDataServiceImpl implements GenesisDataService {

  @Value("${genesis.file}")
  String fileGenesis;

  final ObjectMapper objectMapper;
  final BlockRepository blockRepository;
  final SlotLeaderRepository slotLeaderRepository;
  final TxRepository txRepository;
  final TxOutRepository txOutRepository;
  final AddressRepository addressRepository;
  final AddressTxBalanceRepository addressTxBalanceRepository;

  final CostModelService costModelService;
  final EpochParamService epochParamService;

  final ResourceLoader resourceLoader;
  @Transactional
  public void setupData() {
    GenesisData genesisData = getGenesisData(fileGenesis);

    epochParamService.setDefShelleyEpochParam(genesisData.getShelley());
    epochParamService.setDefAlonzoEpochParam(genesisData.getAlonzo());
    // if block table have blocks do not thing
    if (blockRepository.getBlockIdHeight().isPresent()) {
      costModelService.setGenesisCostModel(genesisData.getCostModel());
      return;
    }

    List<SlotLeader> slotLeaders = slotLeaderRepository.saveAll(
        genesisData.getSlotLeaders()
            .stream()
            .sorted(Comparator.comparing(BaseEntity::getId))
            .toList());
    log.info("Inserted slot leader size {}", slotLeaders.size());

    List<Block> blocks = blockRepository.saveAll(
        genesisData.getBlocks().stream()
            .map(block -> {
              block.setSlotLeader(slotLeaders.get(getPosition(block.getSlotLeaderId())));
              return block;
            })
            .sorted(Comparator.comparing(BaseEntity::getId))
            .toList());
    log.info("Inserted block size {}", blocks.size());

    List<Tx> txs = txRepository.saveAll(
        genesisData.getTxs().stream().map(
                tx -> {
                  tx.setBlock(blocks.get(getPosition(tx.getBlockId())));
                  return tx;
                })
            .sorted(Comparator.comparing(BaseEntity::getId))
            .toList());
    log.info("Inserted transaction size {}", txs.size());

    List<TxOut> txOuts = txOutRepository.saveAll(genesisData.getTxOuts().stream()
        .map(txOut -> {
          txOut.setTx(
              txs.get(getPosition(txOut.getTxId())));
          return txOut;
        })
        .sorted(
            Comparator.comparing(BaseEntity::getId))
        .toList());
    log.info("Inserted transaction output size {}", txOuts.size());

    costModelService.setGenesisCostModel(genesisData.getCostModel());
    log.info("Inserted cost model");

    log.info("Inserted address transaction balance size {} ",
        handleGenesisAddressTxBalance(txOuts).size());
  }

  private List<AddressTxBalance> handleGenesisAddressTxBalance(List<TxOut> txOuts) {

    Collection<AddressTxBalance> addressTxBalances = txOuts.stream()
        .map(txOut -> AddressTxBalance.builder()
            .address(Address.builder()
                .address(txOut.getAddress())
                .addressHasScript(Boolean.FALSE)
                .balance(txOut.getValue())
                .txCount(BigInteger.ONE.longValue())
                .build())
            .tx(txOut.getTx())
            .balance(txOut.getValue())
            .time(txOut.getTx().getBlock().getTime())
            .build())
        .map(AddressTxBalance.class::cast)
        .collect(Collectors.toMap(addressTxBalance ->
                Pair.of(addressTxBalance.getAddress().getAddress(),
                    addressTxBalance.getTx().getId()),
            Function.identity(), (oldEntity, newEntity) -> {
              final BigInteger currentBalance = newEntity.getBalance();
              final BigInteger newBalance = newEntity.getBalance();
              final BigInteger finalBalance = currentBalance.add(newBalance);
              newEntity.getAddress().setBalance(finalBalance);
              newEntity.setBalance(finalBalance);
              return newEntity;
            }))
        .values()
        .stream()
        .sorted(Comparator.comparing(AddressTxBalance::getTime))
        .toList();

    Map<String, Address> genesisAddress = handleGenesisAddress(addressTxBalances)
        .stream().collect(Collectors.toMap(Address::getAddress, Function.identity()));

    log.info("Inserted address size {} ", genesisAddress.size());

    // map address entity
    return addressTxBalanceRepository
        .saveAll(addressTxBalances.stream()
            .map(addressTxBalance -> {

              Address address = addressTxBalance.getAddress();
              addressTxBalance.setAddress(address);
              return addressTxBalance;
            })
            .sorted(Comparator.comparing(o -> o.getTx().getId())).toList());
  }

  private List<Address> handleGenesisAddress(Collection<AddressTxBalance> addressTxBalances) {
    // pair of address and tx_id
    Map<Pair<String, Long>, Address> addresses = addressTxBalances.stream()
        .collect(
            Collectors.toMap(addressTxBalance -> Pair.of(addressTxBalance.getAddress().getAddress(),
                    addressTxBalance.getTx().getId()),
                AddressTxBalance::getAddress,
                (oldEntity, newEntity) -> {
                  final BigInteger currentBalance = newEntity.getBalance();
                  final BigInteger newBalance = newEntity.getBalance();
                  final BigInteger finalBalance = currentBalance.add(newBalance);

                  final long currentTotalTx =
                      Objects.isNull(oldEntity.getTxCount())
                      ? BigInteger.ZERO.longValue()
                      : oldEntity.getTxCount();

                  newEntity.setBalance(finalBalance);
                  newEntity.setTxCount(currentTotalTx + BigInteger.ONE.longValue());
                  return newEntity;
                }));

    // order address by tx_id then insert
    return addressRepository.saveAll(addresses.keySet()
        .stream()
        .sorted(Comparator.comparing(Pair::getSecond))
        .map(addresses::get)
        .toList());
  }

  private GenesisData getGenesisData(String fileName) {
    GenesisData data = null;
    try {
      StringBuilder genesisJson = new StringBuilder();
      try (InputStream fileGenesis = resourceLoader.getResource(fileName).getInputStream()) {
        byte[] bytes = new byte[500];
        while (fileGenesis.available() != 0) {
          fileGenesis.read(bytes);
          genesisJson.append(new String(bytes));
        }
      } catch (Exception exception) {
        log.error("[readFile] file error {}", fileName);
      }
      data = objectMapper.readValue(genesisJson.toString(), GenesisData.class);
    } catch (JsonProcessingException e) {
      log.error("Genesis data at {} can't parse from json to java object", fileGenesis);
      log.error("{}", e.getMessage());
      System.exit(0);
    }

    data.getTxs().sort(Comparator.comparing(Tx::getId));
    data.getTxOuts().sort(Comparator.comparing(TxOut::getId));
    data.getSlotLeaders().sort(Comparator.comparing(SlotLeader::getId));
    return data;
  }

  private Integer getPosition(Long id) {
    return id.intValue() - BigInteger.ONE.intValue();
  }

}
