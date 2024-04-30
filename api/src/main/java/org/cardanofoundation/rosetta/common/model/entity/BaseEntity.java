package org.cardanofoundation.rosetta.common.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import lombok.Data;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@Data
public class BaseEntity {

    @CreationTimestamp
    @Column(name = "create_datetime")
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    @Column(name = "update_datetime")
    private LocalDateTime updateDateTime;

}
