package org.cardanofoundation.rosetta.crawler.repository;


import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotLeaderRepository extends JpaRepository<SlotLeader, Long> {
}