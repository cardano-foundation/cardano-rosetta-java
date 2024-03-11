package org.cardanofoundation.rosetta.api.block.model.dto;

import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessOperationsDto {
    private ArrayList<TransactionInput> transactionInputs=new ArrayList<>();
    private ArrayList<TransactionOutput> transactionOutputs=new ArrayList<>();
    private ArrayList<Certificate> certificates=new ArrayList<>();
    private ArrayList<Withdrawal> withdrawals=new ArrayList<>();
    private ArrayList<String> addresses=new ArrayList<>();
    private ArrayList<String> inputAmounts=new ArrayList<>();
    private ArrayList<String> outputAmounts=new ArrayList<>();
    private ArrayList<Long> withdrawalAmounts=new ArrayList<>();
    private double stakeKeyRegistrationsCount=0.0;
    private double stakeKeyDeRegistrationsCount=0.0;
    private double poolRegistrationsCount=0.0;
    private AuxiliaryData voteRegistrationMetadata=null;
}
