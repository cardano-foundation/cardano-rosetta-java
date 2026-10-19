package org.cardanofoundation.rosetta.api.error.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.UpdateTimestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "error")
public class ErrorEntity {

    @Id
    private Integer id;

    @Column(name = "block")
    private Long block;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "reason")
    private String reason;

    @Column(name = "details")
    private String details;

    @UpdateTimestamp
    @Column(name = "update_datetime")
    private LocalDateTime updateDateTime;

}
