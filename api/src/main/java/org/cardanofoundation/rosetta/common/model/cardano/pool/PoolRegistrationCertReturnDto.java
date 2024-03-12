package org.cardanofoundation.rosetta.common.model.cardano.pool;

import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PoolRegistrationCertReturnDto {

  private PoolRegistration certificate;
  private Set<String> address;
}
