package org.cardanofoundation.rosetta.common.model.cardano.pool;

import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
public class PoolRetirementDto {

  private Certificate certificate;
  private String poolKeyHash;

}
