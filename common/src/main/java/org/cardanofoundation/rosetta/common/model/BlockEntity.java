package org.cardanofoundation.rosetta.common.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.cglib.core.Block;

import java.math.BigInteger;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "block")
public class BlockEntity extends BaseEntity {
    @Id
    @Column(name = "hash")
    private String hash;

    @Column(name = "number")
    private Long number;

    @Column(name = "slot")
    private Long slot;

    @Column(name = "epoch")
    private Integer epochNumber;

    @Column(name = "epoch_slot")
    private Integer epochSlot;

    @Column(name = "total_output")
    private BigInteger totalOutput;

    @Column(name = "total_fees")
    private BigInteger totalFees;

    @Column(name = "block_time")
    private Long blockTime;

    @Column(name = "era")
    private Integer era;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prev_hash",
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
    @EqualsAndHashCode.Exclude
    private BlockEntity prev;

    @Column(name = "issuer_vkey")
    private String issuerVkey;

    @Column(name = "vrf_vkey")
    private String vrfVkey;

//    @Type(JsonType.class)
//    @Column(name = "nonce_vrf")
//    private Vrf nonceVrf;
//
//    @Type(JsonType.class)
//    @Column(name="leader_vrf")
//    private Vrf leaderVrf;
//
//    @Type(JsonType.class)
//    @Column(name="vrf_result")
//    private Vrf vrfResult;

    @Column(name = "op_cert_hot_vkey")
    private String opCertHotVKey;

    @Column(name = "op_cert_seq_number")
    private Integer opCertSeqNumber;

    @Column(name = "op_cert_kes_period")
    private Integer opcertKesPeriod;

    @Column(name = "op_cert_sigma")
    private String opCertSigma;

    @Column(name = "body_size")
    private Long blockBodySize;

    @Column(name="body_hash")
    private String blockBodyHash;

    @Column(name = "protocol_version")
    private String protocolVersion;

    @Column(name = "no_of_txs")
    private Long noOfTxs;

    @Column(name = "slot_leader")
    private String slotLeader;
}
