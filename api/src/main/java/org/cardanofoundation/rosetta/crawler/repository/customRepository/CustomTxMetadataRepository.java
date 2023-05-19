package org.cardanofoundation.rosetta.crawler.repository.customRepository;

import java.util.List;
import org.cardanofoundation.rosetta.crawler.projection.dto.TransactionMetadataDto;

public interface CustomTxMetadataRepository {
  List<TransactionMetadataDto> findTransactionMetadata(List<byte[]> hashes);

}
