package org.cardanofoundation.rosetta.api.repository.customRepository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystLabels;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionMetadataDto;
import org.cardanofoundation.rosetta.api.repository.customRepository.CustomTxMetadataRepository;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@NoArgsConstructor
@AllArgsConstructor
public class CustomTxMetadataRepositoryImpl implements CustomTxMetadataRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<TransactionMetadataDto> findTransactionMetadata(List<byte[]> hashes) {
//    String findTransactionMetadataQuery = "WITH metadata AS ( "
//        + "  SELECT metadata.json, metadata.key, tx.hash AS txHash, tx.id AS txId "
//        + "  FROM TxMetadata AS metadata "
//        + "  JOIN Tx AS tx ON tx.id = metadata.tx.id "
//        + "  WHERE tx.hash IN :hashList "
//        + "), "
//        + "metadata_data AS ( "
//        + "  SELECT json AS data, metadata.txHash, metadata.txId "
//        + "  FROM metadata "
//        + "  WHERE metadata.key = " + CatalystLabels.DATA
//        + " ), "
//        + "metadata_sig AS ( "
//        + "  SELECT json AS signature, metadata.txHash, metadata.txId "
//        + "  FROM metadata "
//        + "  WHERE metadata.key =  " + CatalystLabels.SIG
//        + " ) "
//        + "SELECT data, signature, metadata_data.txHash "
//        + "FROM metadata_data "
//        + "INNER JOIN metadata_sig "
//        + "  ON metadata_data.tx.id = metadata_sig.tx.id ";
    byte[][] finalHashes = new byte[hashes.size()][];
    for (int i = 0; i < hashes.size(); i++) {
      finalHashes[i] = hashes.get(i);
    }
    String findTransactionMetadataQuery = "WITH metadata AS ( "
        + "  SELECT "
        + "    metadata.json as json, "
        + "    metadata.key as key, "
        + "    tx.hash AS txHash, "
        + "    tx.id AS txId "
        + "  FROM tx_metadata AS metadata "
        + "  JOIN tx "
        + "    ON tx.id = metadata.tx_id "
        + "  WHERE tx.hash = ANY( :hashList ) "
        + "  ), "
        + "  metadata_data AS ( "
        + "    SELECT  "
        + "      metadata.json AS data, "
        + "      metadata.txHash AS txHash, "
        + "      metadata.txId AS txId "
        + "    FROM metadata "
        + "    WHERE key =  " + CatalystLabels.DATA.getLabel()
        + "  ), "
        + "  metadata_sig AS ( "
        + "    SELECT  "
        + "      metadata.json AS signature, "
        + "      metadata.txHash AS txHash, "
        + "      metadata.txId AS txId "
        + "    FROM metadata "
        + "    WHERE key =  " + CatalystLabels.SIG.getLabel()
        + "  ) "
        + "  SELECT  "
        + "    metadata_sig.signature AS signature , "
        + "    metadata_data.data AS data, "
        + "    metadata_data.txHash AS txHash "
        + "  FROM metadata_data "
        + "  INNER JOIN metadata_sig "
        + "    ON metadata_data.txId = metadata_sig.txId ";
    List<TransactionMetadataDto> transactionMetadataDtos = entityManager
        .createNativeQuery(findTransactionMetadataQuery)
        .setParameter("hashList", finalHashes)
        .unwrap(org.hibernate.query.NativeQuery.class)
        .setTupleTransformer(Transformers.aliasToBean(TransactionMetadataDto.class))
        .getResultList();
    return transactionMetadataDtos;
  }
}
