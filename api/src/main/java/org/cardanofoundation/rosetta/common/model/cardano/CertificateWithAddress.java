package org.cardanofoundation.rosetta.common.model.cardano;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;

@Slf4j
@Data
@AllArgsConstructor
public class CertificateWithAddress {

  private Certificate certificate;
  private String address;

}
