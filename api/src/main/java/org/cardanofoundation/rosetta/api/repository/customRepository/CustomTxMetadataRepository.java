package org.cardanofoundation.rosetta.api.repository.customRepository;

import java.util.List;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionMetadataDto;

public interface CustomTxMetadataRepository {
  List<TransactionMetadataDto> findTransactionMetadata(List<byte[]> hashes);

}
