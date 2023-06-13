package org.cardanofoundation.rosetta.api.config.yaci;

import com.bloxbean.cardano.yaci.core.protocol.localtx.LocalTxSubmissionListener;
import com.bloxbean.cardano.yaci.core.protocol.localtx.messages.MsgAcceptTx;
import com.bloxbean.cardano.yaci.core.protocol.localtx.messages.MsgRejectTx;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalStateQueryClient;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@EnableConfigurationProperties(value = {CardanoTransactionSubmitterProperties.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class YaciConfiguration {

  CardanoTransactionSubmitterProperties cardanoTransactionSubmitterProperties;
  public YaciConfiguration(
      CardanoTransactionSubmitterProperties cardanoTransactionSubmitterProperties) {
    this.cardanoTransactionSubmitterProperties = cardanoTransactionSubmitterProperties;
  }

  @Bean
  LocalClientProvider localClientProvider() throws RuntimeException {
    var socketPath = cardanoTransactionSubmitterProperties.getConnection().getSocket().getPath();
    var networkMagic = cardanoTransactionSubmitterProperties.getNetworkMagic();

    LocalClientProvider localClientProvider = new LocalClientProvider(socketPath, networkMagic);
    //Start localClientProvider
    localClientProvider.addTxSubmissionListener(new LocalTxSubmissionListener() {
      @Override
      public void txAccepted(TxSubmissionRequest txSubmissionRequest, MsgAcceptTx msgAcceptTx) {
        log.info("TxId : " + txSubmissionRequest.getTxHash());
      }
      @Override
      public void txRejected(TxSubmissionRequest txSubmissionRequest, MsgRejectTx msgRejectTx) {
        String reasonCbor = msgRejectTx.getReasonCbor();
        log.info("Rejected: " + reasonCbor);
      }
    });
    localClientProvider.start();
    return localClientProvider;
  }

  @Bean
  @DependsOn("localClientProvider")
  LocalStateQueryClient localStateQueryClient(@Autowired LocalClientProvider localClientProvider){
    return localClientProvider.getLocalStateQueryClient();
  }

  @Bean
  @DependsOn("localClientProvider")
  LocalTxSubmissionClient localTxSubmissionClient(@Autowired LocalClientProvider localClientProvider){
    return localClientProvider.getTxSubmissionClient();
  }
}
