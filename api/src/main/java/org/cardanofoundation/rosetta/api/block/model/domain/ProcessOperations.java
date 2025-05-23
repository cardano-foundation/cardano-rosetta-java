package org.cardanofoundation.rosetta.api.block.model.domain;

import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import com.bloxbean.cardano.client.transaction.spec.governance.VotingProcedures;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessOperations {

  private ArrayList<TransactionInput> transactionInputs = new ArrayList<>();
  private ArrayList<TransactionOutput> transactionOutputs = new ArrayList<>();
  private ArrayList<Certificate> certificates = new ArrayList<>();
  private ArrayList<Withdrawal> withdrawals = new ArrayList<>();
  private ArrayList<String> addresses = new ArrayList<>();
  private ArrayList<BigInteger> inputAmounts = new ArrayList<>();
  private ArrayList<BigInteger> outputAmounts = new ArrayList<>();
  private ArrayList<BigInteger> withdrawalAmounts = new ArrayList<>();
  private double stakeKeyRegistrationsCount = 0.0;
  private double stakeKeyDeRegistrationsCount = 0.0;
  private double poolRegistrationsCount = 0.0;
  private AuxiliaryData voteRegistrationMetadata = null;
  private VotingProcedures votingProcedures = new VotingProcedures();

}
