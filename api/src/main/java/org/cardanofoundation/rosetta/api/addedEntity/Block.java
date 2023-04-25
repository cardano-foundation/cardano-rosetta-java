package org.cardanofoundation.rosetta.api.addedEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "block")
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "block_no")
    private String blockNo;

}
