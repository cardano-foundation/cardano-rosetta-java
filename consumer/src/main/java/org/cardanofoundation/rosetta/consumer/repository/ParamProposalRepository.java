package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamProposalRepository extends JpaRepository<ParamProposal, Long> {

  List<ParamProposal> findParamProposalsByEpochNo(Integer epochNo);
}
