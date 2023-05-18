package org.cardanofoundation.rosetta.consumer.factory;

import jakarta.annotation.PostConstruct;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.Certificate;
import org.cardanofoundation.rosetta.consumer.service.CertificateSyncService;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CertificateSyncServiceFactory extends
    AbstractServiceFactory<CertificateSyncService<? extends Certificate>, CertificateSyncService> {// NOSONAR


  public CertificateSyncServiceFactory(
      List<CertificateSyncService<? extends Certificate>> certificateSyncServices) {
    super(certificateSyncServices);
  }

  @PostConstruct
  void init() {
    serviceMap = services.stream()
        .collect(
            Collectors.toMap(
                CertificateSyncService::supports,
                Function.identity()));

  }

  @SuppressWarnings("unchecked")
  public void handle(AggregatedBlock aggregatedBlock,
      Certificate certificate, int certificateIdx, Tx tx, Redeemer redeemer) {
    serviceMap.get(certificate.getClass()).handle(
        aggregatedBlock, certificate, certificateIdx, tx, redeemer);
  }
}
