package org.cardanofoundation.rosetta.common.model.cardano.pool;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PoolRegistrationCertReturn {

  private PoolRegistration certificate;
  private Set<String> address;
}
