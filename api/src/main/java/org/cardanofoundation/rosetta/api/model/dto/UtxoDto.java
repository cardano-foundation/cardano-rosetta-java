package org.cardanofoundation.rosetta.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.model.entity.Amt;
import org.cardanofoundation.rosetta.api.model.entity.UtxoKey;

import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UtxoDto {
    private String txHash;
    private Integer outputIndex;
    private Long slot;
    private String blockHash;
    private Integer epoch;
    private String ownerAddr;
    private String ownerAddrFull;
    private String ownerStakeAddr;
    private String ownerPaymentCredential;
    private String ownerStakeCredential;
    private BigInteger lovelaceAmount;
    private List<Amt> amounts;
    private String dataHash;
    private String inlineDatum;
    private String scriptRef;
    private String referenceScriptHash;
    private Boolean isCollateralReturn;

    public UtxoDto(String txHash, Integer outputIndex) {
        this.txHash = txHash;
        this.outputIndex = outputIndex;
    }

    public static UtxoDto fromUtxoKey(UtxoKey utxoKey) {
        return new UtxoDto(utxoKey.getTxHash(), utxoKey.getOutputIndex());
    }

    public void populateFromUtxoEntity(AddressUtxoEntity entity) {
        this.slot = entity.getSlot();
        this.blockHash = entity.getBlockHash();
        this.epoch = entity.getEpoch();
        this.ownerAddr = entity.getOwnerAddr();
        this.ownerAddrFull = entity.getOwnerAddrFull();
        this.ownerStakeAddr = entity.getOwnerStakeAddr();
        this.ownerPaymentCredential = entity.getOwnerPaymentCredential();
        this.ownerStakeCredential = entity.getOwnerStakeCredential();
        this.lovelaceAmount = entity.getLovelaceAmount();
        this.amounts = entity.getAmounts();
        this.dataHash = entity.getDataHash();
        this.inlineDatum = entity.getInlineDatum();
        this.scriptRef = entity.getScriptRef();
        this.referenceScriptHash = entity.getReferenceScriptHash();
        this.isCollateralReturn = entity.getIsCollateralReturn();
    }
}
