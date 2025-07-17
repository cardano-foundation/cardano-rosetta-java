package org.cardanofoundation.rosetta.api.block.model.domain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessOperations {

  private List<TransactionInput> transactionInputs = new ArrayList<>();
  private List<TransactionOutput> transactionOutputs = new ArrayList<>();
  private List<Certificate> certificates = new ArrayList<>();
  private List<Withdrawal> withdrawals = new ArrayList<>();
  private List<String> addresses = new ArrayList<>();
  private List<BigInteger> inputAmounts = new ArrayList<>();
  private List<BigInteger> outputAmounts = new ArrayList<>();
  private List<BigInteger> withdrawalAmounts = new ArrayList<>();
  private List<GovernancePoolVote> governancePoolVotes = new ArrayList<>();

  private double stakeKeyRegistrationsCount = 0.0;
  private double stakeKeyDeRegistrationsCount = 0.0;
  private double poolRegistrationsCount = 0.0;
  private AuxiliaryData voteRegistrationMetadata = null;

}
