package org.cardanofoundation.rosetta.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionExtraData {
    private List<Operation> operations;
    private String transactionMetadataHex;

}
