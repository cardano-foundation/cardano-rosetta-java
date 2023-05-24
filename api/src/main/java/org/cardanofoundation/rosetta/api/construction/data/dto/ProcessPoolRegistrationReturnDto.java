package org.cardanofoundation.rosetta.api.construction.data.dto;

import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProcessPoolRegistrationReturnDto {

  private List<String> totalAddresses;
  private PoolRegistration certificate;

}
