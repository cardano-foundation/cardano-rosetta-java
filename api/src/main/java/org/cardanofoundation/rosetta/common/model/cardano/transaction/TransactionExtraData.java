package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.client.model.Operation;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionExtraData {
    private List<Operation> operations;
    private String transactionMetadataHex;

}
