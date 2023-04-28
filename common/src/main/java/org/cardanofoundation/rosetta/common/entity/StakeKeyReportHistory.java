package org.cardanofoundation.rosetta.common.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.enumeration.StakeKeyReportStatus;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "stake_key_report_history",  uniqueConstraints = {
    @UniqueConstraint(name = "unique_report_name",
        columnNames = {"report_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StakeKeyReportHistory extends BaseEntity {

  @Column(name = "stake_key", nullable = false)
  private String stakeKey;

  @Column(name = "report_name", nullable = false)
  private String reportName;

  @Column(name = "from_date", nullable = false)
  private Timestamp fromDate;

  @Column(name = "to_date", nullable = false)
  private Timestamp toDate;

  @Column(name = "is_ada_transfer", nullable = false)
  private Boolean isADATransfer;

  @Column(name = "is_fees_paid", nullable = false)
  private Boolean isFeesPaid;

  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private StakeKeyReportStatus status;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "stake_key_report_history_event",
      joinColumns = @JoinColumn(name = "report_history_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "event_type", referencedColumnName = "event_type"))
  @EqualsAndHashCode.Exclude
  private Set<StakingLifeCycleEvent> stakingLifeCycleEvents;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    StakeKeyReportHistory that = (StakeKeyReportHistory) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
