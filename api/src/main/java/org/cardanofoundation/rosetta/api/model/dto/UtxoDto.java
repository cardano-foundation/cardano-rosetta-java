package org.cardanofoundation.rosetta.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.common.model.UtxoKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UtxoDto {
    private String txHash;
    private Integer outputIndex;

    public static UtxoDto fromUtxoKey(UtxoKey utxoKey) {
        return new UtxoDto(utxoKey.getTxHash(), utxoKey.getOutputIndex());
    }
}
