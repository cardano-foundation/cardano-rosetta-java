package org.cardanofoundation.rosetta.api.projection.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PopulatedTransaction extends TransactionDto {

  private List<TransactionInput> inputs;
  private List<TransactionOutput> outputs;
  private List<Withdrawal> withdrawals;
  private List<Registration> registrations;
  private List<Deregistration> deregistrations;
  private List<Delegation> delegations;
  private List<PoolRegistration> poolRegistrations;
  private List<PoolRetirement> poolRetirements;
  private List<VoteRegistration> voteRegistrations;

}
