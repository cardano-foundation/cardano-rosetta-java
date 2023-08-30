package org.cardanofoundation.rosetta.api.projection.dto;

import com.bloxbean.cardano.client.address.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProcessWithdrawalReturnDto {

  private Address reward;
  private String address;

}
