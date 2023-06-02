package org.cardanofoundation.rosetta.api.repository.customrepository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystLabels;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionMetadataDto;
import org.cardanofoundation.rosetta.api.repository.customrepository.CustomTxMetadataRepository;
import org.hibernate.query.TupleTransformer;
import org.hibernate.transform.ResultTransformer;
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
  public List<TransactionMetadataDto> findTransactionMetadata(List<String> hashes) {
    String[] finalHashes = new String[hashes.size()];
    for (int i = 0; i < hashes.size(); i++) {
      finalHashes[i] = new String(hashes.get(i));
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
        + "    metadata_data.txHash AS txHash"
        + "  FROM metadata_data "
        + "  INNER JOIN metadata_sig "
        + "    ON metadata_data.txId = metadata_sig.txId ";
    return entityManager
        .createNativeQuery(findTransactionMetadataQuery)
        .setParameter("hashList", finalHashes)
        .unwrap(org.hibernate.query.NativeQuery.class)
        .setTupleTransformer((tuples, aliases) -> {
          TransactionMetadataDto transactionMetadataDto = new TransactionMetadataDto();
          transactionMetadataDto.setSignature((String) tuples[0]);
          transactionMetadataDto.setData((String) tuples[1]);
          transactionMetadataDto.setTxHash((String) tuples[2]);
          return transactionMetadataDto;
        })
        .setResultListTransformer(resultList -> resultList)
        .getResultList();
  }
}
