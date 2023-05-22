package org.cardanofoundation.rosetta.api.construction.data.dto;

import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PoolRegistrationCertReturnDto {

  private PoolRegistration certificate;
  private Set<String> address;
}
