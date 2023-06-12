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

    String findTransactionMetadataQuery =
        "SELECT metadata_sig.signature AS signature,"
            + "metadata_data.data AS data, "
            + "metadata_data.txHash AS txHash "
            + "FROM ( "
            + "SELECT metadata.json AS data,"
            + "tx.hash AS txHash, "
            + "tx.id AS txId "
            + "FROM TxMetadata AS metadata "
            + "JOIN Tx tx "
            + "ON tx.id = metadata.tx.id "
            + "WHERE tx.hash IN (:hashList) "
            + "AND metadata.key = " + CatalystLabels.DATA.getLabel()
            + " ) metadata_data "
            + "JOIN "
            + " ( "
            + "SELECT metadata.json AS signature,"
            + "tx.hash AS txHash, "
            + "tx.id AS txId "
            + "FROM TxMetadata AS metadata "
            + "JOIN Tx tx "
            + "ON tx.id = metadata.tx.id "
            + "WHERE tx.hash IN (:hashList)"
            + " AND metadata.key = " + CatalystLabels.SIG.getLabel()
            + ") metadata_sig ON  metadata_data.txId = metadata_sig.txId ";
    return entityManager
        .createQuery(findTransactionMetadataQuery)
        .setParameter("hashList", hashes)
        .unwrap(org.hibernate.query.Query.class)
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

