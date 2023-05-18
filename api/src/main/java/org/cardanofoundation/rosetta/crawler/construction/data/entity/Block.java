package org.cardanofoundation.rosetta.crawler.construction.data.entity;

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
