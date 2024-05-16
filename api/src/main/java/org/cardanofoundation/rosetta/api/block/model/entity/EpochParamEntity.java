package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "epoch_param")
public class EpochParamEntity {

  @Id
  @Column(name = "epoch")
  private Integer epoch;

  @Type(JsonType.class)
  @Column(name = "params", columnDefinition = "json")
  private ProtocolParamsEntity params;

}
