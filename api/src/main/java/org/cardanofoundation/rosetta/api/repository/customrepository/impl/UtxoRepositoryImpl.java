package org.cardanofoundation.rosetta.api.repository.customrepository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.CurrencyId;
import org.cardanofoundation.rosetta.api.model.rest.FindMaBalance;
import org.cardanofoundation.rosetta.api.model.rest.FindUtxo;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.repository.customrepository.UtxoRepository;
import org.cardanofoundation.rosetta.api.util.Formatters;
import org.hibernate.transform.Transformers;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class UtxoRepositoryImpl implements UtxoRepository {

  public static final String POLICYID = "policyId";
  public static final String UTXO_QUERY = buildUtxoQuery();
  @PersistenceContext
  private EntityManager entityManager;

  @Query("SELECT asset.name as name, "
      + "asset.policy as policy, "
      + "SUM(maTxOut.quantity) as value "
      + "FROM ( "
      + "${utxoQuery}"
      + " ) AS utxo "
      + "LEFT JOIN MaTxOut maTxOut ON maTxOut.txOut.id = utxo.txOutId "
      + "JOIN MultiAsset asset ON asset.id = maTxOut.ident.id "
      + "WHERE asset.policy IS NOT NULL "
      + "GROUP BY asset.name, asset.policy "
      + "ORDER BY asset.policy, asset.name")

  public static String buildFindMaBalanceByAddressAndBlock() {
    try {
      log.debug("FindMaBalanceByAddressAndBlock query is : " + UtxoRepositoryImpl.class
          .getMethod("buildFindMaBalanceByAddressAndBlock", null)
          .getAnnotation(Query.class).value());
      String query = UtxoRepositoryImpl.class
          .getMethod("buildFindMaBalanceByAddressAndBlock", null)
          .getAnnotation(Query.class).value();
      Map<String, Object> values = new HashMap<>();
      values.put("utxoQuery", UTXO_QUERY);
      return format(query, values);

    } catch (NoSuchMethodException e) {

      throw new RuntimeException(e);

    }
  }

  @Query("SELECT txOut.value as value, "
      + "txOutTx.hash as txHash, "
      + "txOut.index as index, "
      + "txOut.id as txOutId "
      + "FROM TxOut txOut "
      + "LEFT JOIN TxIn txIn ON "
      + "   txOut.txId = txIn.txOut.id "
      + "   AND txOut.index = txIn.txOutIndex"
      + " LEFT JOIN Tx txInTx ON txInTx.id = txIn.txInput.id "
      + "   AND txInTx.blockId <= (SELECT id FROM Block WHERE hash = :hash)"
      + "   AND txInTx.validContract = true "
      + "JOIN Tx txOutTx ON txOutTx.id = txOut.tx.id "
      + "   AND txOutTx.blockId <= (SELECT id FROM Block WHERE hash = :hash) "
      + "   AND txOutTx.validContract = true "
      + "WHERE txOut.address = :address "
      + "   AND txInTx.id IS NULL")
  public static String buildUtxoQuery() {
    try {
      return UtxoRepositoryImpl.class
          .getMethod("buildUtxoQuery", null)
          .getAnnotation(Query.class).value();
    } catch (NoSuchMethodException e) {

      throw new RuntimeException(e);

    }
  }

  public static String buildWhereCurrenciesClause(List<CurrencyId> currencies) {
    return (Objects.nonNull(currencies) && !currencies.isEmpty())
        ? "WHERE utxo.txOutId In ( " + buildCurrenciesQuery(currencies) + " )"
        : "";
  }

  public static String buildCurrenciesQuery(List<CurrencyId> currencies) {
    String whereClause = IntStream.range(0, currencies.size())
        .mapToObj(i -> "( asset.name = " + ":symbol" + i
            + " AND asset.policy = " + ":policy" + i + " )")
        .collect(Collectors.joining(" OR "));
    return "SELECT maTxOut.txOut.id "
        + "FROM MaTxOut as maTxOut "
        + "JOIN MultiAsset as asset "
        + "ON asset.id = maTxOut.ident.id "
        + "WHERE " + whereClause;
  }


  public static List<Utxo> mapUtxos(List<FindUtxo> findUtxos) {
    return findUtxos.stream().map(findUtxo -> new Utxo(
        findUtxo.getValue().toString(),
        findUtxo.getTxHash(),
        Integer.valueOf(findUtxo.getIndex()),
        findUtxo.getName(),
        findUtxo.getPolicy(),
        Objects.nonNull(findUtxo.getQuantity()) ? findUtxo.getQuantity().toString() : null
    )).collect(Collectors.toList());
  }

  public static String format(String format, Map<String, Object> values) {
    StringBuilder formatter = new StringBuilder(format);
    List<Object> valueList = new ArrayList<>();

    Matcher matcher = Pattern.compile("\\$\\{(\\w+)}").matcher(format);

    while (matcher.find()) {
      String key = matcher.group(1);

      String formatKey = String.format("${%s}", key);
      int index = formatter.indexOf(formatKey);

      if (index != -1) {
        formatter.replace(index, index + formatKey.length(), "%s");
        valueList.add(values.get(key));
      }
    }

    return String.format(formatter.toString(), valueList.toArray());
  }

  @Override
  public List<Utxo> findUtxoByAddressAndBlock(String address,
      String blockHash,
      List<Currency> currencies) {
    log.debug(
        "[findUtxoByAddressAndBlock] About to run findUtxoByAddressAndBlock query with parameters: Address "
            + address + " BlockHash " + blockHash
    );

    List<CurrencyId> currenciesIds = Optional.ofNullable(currencies)
        .orElse(Collections.emptyList())
        .stream()
        .map(currency -> CurrencyId.builder()
            .symbol(Formatters.isEmptyHexString(currency.getSymbol()) ? ""
                : currency.getSymbol())
            .policy(Objects.nonNull(currency.getMetadata()) ? (String) currency.getMetadata()
                .get(POLICYID) : null)
            .build()
        )
        .collect(Collectors.toList());

    log.debug("[findUtxoByAddressAndBlock] currenciesIds is " + currenciesIds);
    log.debug("[findUtxoByAddressAndBlock] Query is " + buildFindUtxoByAddressAndBlockQuery(address,
        blockHash,
        currenciesIds));

    jakarta.persistence.Query query = entityManager
        .createQuery(buildFindUtxoByAddressAndBlockQuery(address,
            blockHash,
            currenciesIds), FindUtxo.class);

    if (Objects.nonNull(currencies)) {
      IntStream.range(0, currencies.size())
          .forEach(i -> {
            query.setParameter("symbol" + i, currenciesIds.get(i).getSymbol());
            query.setParameter("policy" + i, currenciesIds.get(i).getPolicy());

          });
    }
    List<FindUtxo> findUtxos = query
        .setParameter("hash", blockHash)
        .setParameter("address", address)
        .unwrap(org.hibernate.query.Query.class)
        .setTupleTransformer(Transformers.aliasToBean(FindUtxo.class))
        .getResultList();
    log.debug("[findUtxoByAddressAndBlock] Found " + findUtxos.size() + " utxos");

    return mapUtxos(findUtxos);
  }

  @Query("SELECT " +
      "utxo.value as value, " +
      "utxo.txHash as txHash, " +
      "utxo.index as index," +
      "asset.name as name," +
      "asset.policy as policy, " +
      "maxtxout.quantity as quantity " +
      "FROM ( " +
      "${utxoQuery}" +
      " ) AS utxo " +
      "LEFT JOIN MaTxOut as maxtxout ON maxtxout.txOut.id = utxo.txOutId " +
      "LEFT JOIN MultiAsset as asset ON asset.id = maxtxout.ident.id " +
      "${whereClause} " +
      "ORDER BY utxo.txHash, utxo.index, asset.policy, asset.name ")
  public String buildFindUtxoByAddressAndBlockQuery(String address, String hash,
      List<CurrencyId> currencies) {
    try {
      String sb = UtxoRepositoryImpl.class
          .getMethod("buildFindUtxoByAddressAndBlockQuery", String.class, String.class, List.class)
          .getAnnotation(Query.class).value();
      Map<String, Object> values = new HashMap<>();
      values.put("utxoQuery", UTXO_QUERY);
      values.put("whereClause", buildWhereCurrenciesClause(currencies));

      return format(sb, values);

    } catch (NoSuchMethodException e) {

      throw new RuntimeException(e);

    }

  }

  @Override
  public List<MaBalance> findMaBalanceByAddressAndBlock(String address,
      String blockHash) {
    List<FindMaBalance> findMaBalances = entityManager
        .createQuery(buildFindMaBalanceByAddressAndBlock(), FindMaBalance.class)
        .setParameter("address", address)
        .setParameter("hash", blockHash)
        .unwrap(org.hibernate.query.Query.class)
        .setTupleTransformer(Transformers.aliasToBean(FindMaBalance.class))
        .getResultList();

    log.debug("[findMultiAssetByAddressAndBlock] Found balances for " + findMaBalances.size()
        + " multi assets");
    return mapMabalances(findMaBalances);
  }

  public List<MaBalance> mapMabalances(List<FindMaBalance> findMaBalances) {
    return findMaBalances.stream().map(findMaBalance -> MaBalance.builder()
            .name(findMaBalance.getName())
            .policy(findMaBalance.getPolicy())
            .value(findMaBalance.getValue().toString())
            .build())
        .collect(Collectors.toList());
  }

}
