package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProcessOperationsReturn {

  private List<TransactionInput> transactionInputs;
  private List<TransactionOutput> transactionOutputs;
  private List<Certificate> certificates;
  private List<GovernancePoolVote> governancePoolVotes;
  private List<Withdrawal> withdrawals;
  private Set<String> addresses;
  private Long fee;
  private AuxiliaryData voteRegistrationMetadata;

}
