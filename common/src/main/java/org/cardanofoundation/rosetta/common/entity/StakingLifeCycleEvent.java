package org.cardanofoundation.rosetta.common.entity;

import org.cardanofoundation.rosetta.common.enumeration.StakingLifeCycleEventType;

import javax.persistence.*;

@Entity
@Table(name = "staking_life_cycle_event")
public class StakingLifeCycleEvent{

      @Enumerated(EnumType.STRING)
      @Id
      @Column(name = "event_type", nullable = false)
      private StakingLifeCycleEventType eventType;

      public StakingLifeCycleEventType getType() {
          return eventType;
      }

      public void setType(StakingLifeCycleEventType type) {
          this.eventType = type;
      }
}
