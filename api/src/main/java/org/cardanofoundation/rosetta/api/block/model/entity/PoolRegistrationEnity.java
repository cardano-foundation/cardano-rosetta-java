package org.cardanofoundation.rosetta.api.block.model.entity;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.bloxbean.cardano.yaci.core.model.Relay;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "pool_registration")
@IdClass(PoolRegistrationId.class)
@DynamicUpdate
public class PoolRegistrationEnity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "cert_index")
  private int certIndex;

  @Column(name = "pool_id")
  private String poolId;

  @Column(name = "vrf_key")
  private String vrfKeyHash;

  @Column(name = "pledge")
  private BigInteger pledge;

  @Column(name = "cost")
  private BigInteger cost;

  @Column(name = "margin")
  private Double margin;

  @Column(name = "reward_account")
  private String rewardAccount;

  @Type(JsonType.class)
  private Set<String> poolOwners;

  @Type(JsonType.class)
  private List<Relay> relays;

}
