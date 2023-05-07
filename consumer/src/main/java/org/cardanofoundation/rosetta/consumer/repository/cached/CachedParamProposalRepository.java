package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import java.util.List;

public interface CachedParamProposalRepository extends BaseCachedRepository<ParamProposal> {

    List<ParamProposal> findParamProposalEpochNo(int epochNo);
}
