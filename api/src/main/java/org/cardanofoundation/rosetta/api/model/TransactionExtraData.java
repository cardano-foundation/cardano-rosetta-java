package org.cardanofoundation.rosetta.api.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionExtraData {
    private List<Operation> operations;
    private String transactionMetadataHex;

}
