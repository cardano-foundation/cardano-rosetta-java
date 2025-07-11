package org.cardanofoundation.rosetta.api.error.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Entity
@Table(name = "error_review")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorReviewEntity {

    @Id
    @Column(name = "id") // links to primary key of the yaci's store 'error' table
    private Integer id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    @Column(name = "comment")
    @Nullable
    private String comment;

    @Column(name = "checked_by")
    @Nullable
    private String checkedBy;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated; // This should be a timestamp, but for simplicity, we use String here.

}
