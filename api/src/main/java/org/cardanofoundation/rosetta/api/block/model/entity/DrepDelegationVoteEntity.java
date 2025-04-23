package org.cardanofoundation.rosetta.api.block.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.bloxbean.cardano.client.transaction.spec.cert.StakeCredType;
import com.bloxbean.cardano.yaci.core.model.governance.DrepType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "delegation_vote")
@IdClass(DelegationVoteId.class)
public class DrepDelegationVoteEntity {

    @jakarta.persistence.Id
    @Column(name = "tx_hash")
    private String txHash;

    @jakarta.persistence.Id
    @Column(name = "cert_index")
    private long certIndex;

    @Column(name = "slot")
    private Long slot;

    @Column(name = "block")
    private Long blockNumber;

    @Column(name = "block_time")
    private Long blockTime;

    @Column(name = "update_datetime")
    private LocalDateTime updateDateTime;

    @Column(name = "address")
    private String address;

    @Column(name = "drep_hash") // actual drep id as hex hash
    private String drepHash;

    @Column(name = "drep_id") // bech 32
    private String drepId;

    @Column(name = "drep_type")
    @Enumerated(EnumType.STRING)
    private DrepType drepType;

    @Column(name = "credential")
    private String credential;

    @Column(name = "cred_type")
    @Enumerated(EnumType.STRING)
    private StakeCredType credType;

    @Column(name = "epoch")
    private Integer epoch;

}
