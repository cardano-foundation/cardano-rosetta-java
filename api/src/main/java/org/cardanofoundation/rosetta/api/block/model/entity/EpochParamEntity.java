package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "epoch_param")
@Slf4j
public class EpochParamEntity {

  @Id
  @Column(name = "epoch")
  private Integer epoch;

  @Type(JsonType.class)
  @Column(name = "params", columnDefinition = "json")
  private ProtocolParamsEntity params;

  @PrePersist
  public void preSave() {
    if (this.getParams() == null) {
      return;
    }

    //reset these fields
    if (this.getParams().getCostModels() != null) {
      this.getParams().setCostModels(null);
    }
  }
}
