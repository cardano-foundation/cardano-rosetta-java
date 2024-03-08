package org.cardanofoundation.rosetta.api.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction")
public class TxnEntity {
    @Id
    @Column(name = "tx_hash")
    private String txHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "block_hash",
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
    private BlockEntity block;

    @Column(name = "slot")
    private Long slot;

    @Type(JsonType.class)
    @Column(name = "inputs")
    private List<UtxoKey> inputKeys;

    @Type(JsonType.class)
    @Column(name = "outputs")
    private List<UtxoKey> outputKeys;

    @Column(name = "fee")
    private BigInteger fee;

    @Column(name = "ttl")
    private Long ttl;

    @Column(name = "auxiliary_datahash")
    private String auxiliaryDataHash;

    @Column(name = "validity_interval_start")
    private Long validityIntervalStart;

    @Column(name = "script_datahash")
    private String scriptDataHash;

    @OneToMany(mappedBy = "txHash")
    private List<TxScriptEntity> script;

    @Type(JsonType.class)
    @Column(name = "collateral_inputs")
    private List<UtxoKey> collateralInputs;

    @Type(JsonType.class)
    @Column(name = "required_signers")
    private Set<String> requiredSigners;

    @Column(name = "network_id")
    private Integer netowrkId;

    @Type(JsonType.class)
    @Column(name = "collateral_return")
    private UtxoKey collateralReturn;

    @Type(JsonType.class)
    @Column(name = "collateral_return_json")
    private TxOuput collateralReturnJson;

    @Column(name = "total_collateral")
    private BigInteger totalCollateral;

    @Type(JsonType.class)
    @Column(name = "reference_inputs")
    private List<UtxoKey> referenceInputs;

    @Column(name = "invalid")
    private Boolean invalid;

    @UpdateTimestamp
    @Column(name = "update_datetime")
    private LocalDateTime updateDateTime;
}
