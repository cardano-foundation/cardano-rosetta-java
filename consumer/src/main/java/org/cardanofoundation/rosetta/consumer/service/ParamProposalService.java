package org.cardanofoundation.rosetta.consumer.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;

public interface ParamProposalService {

  /**
   * Handle CDDL param proposals
   *
   * @param successTxs collection of success txs
   * @param txMap      a map with key is tx hash and value is the respective tx entity
   * @return a list of handled param proposal entities
   */
  List<ParamProposal> handleParamProposals(
      Collection<AggregatedTx> successTxs, Map<String, Tx> txMap);
}
