//package org.cardanofoundation.rosetta.api.addedClass;
//
//import co.nstant.in.cbor.model.DataItem;
//import com.bloxbean.cardano.client.util.HexUtil;
//import com.bloxbean.cardano.yaci.core.protocol.localtx.LocalTxSubmissionListener;
//import com.bloxbean.cardano.yaci.core.protocol.localtx.messages.MsgAcceptTx;
//import com.bloxbean.cardano.yaci.core.protocol.localtx.messages.MsgRejectTx;
//import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
//import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
//import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
//import com.bloxbean.cardano.yaci.helper.LocalStateQueryClient;
//import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
//import lombok.AccessLevel;
//import lombok.experimental.FieldDefaults;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.DependsOn;
//
//@Configuration
//@EnableConfigurationProperties(value = {CardanoTransactionSubmitterProperties.class})
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class YaciConfiguration {
//  CardanoTransactionSubmitterProperties cardanoTransactionSubmitterProperties;
//  public YaciConfiguration(
//      CardanoTransactionSubmitterProperties cardanoTransactionSubmitterProperties) {
//    this.cardanoTransactionSubmitterProperties = cardanoTransactionSubmitterProperties;
//  }
//
//  @Bean
//  LocalClientProvider localClientProvider(){
//    var socketPath = cardanoTransactionSubmitterProperties.getConnection().getSocket().getPath();
//    var networkMagic = cardanoTransactionSubmitterProperties.getNetworkMagic();
//
//    LocalClientProvider localClientProvider = new LocalClientProvider(socketPath, networkMagic);
//    //Start localClientProvider
//    localClientProvider.addTxSubmissionListener(new LocalTxSubmissionListener() {
//      @Override
//      public void txAccepted(TxSubmissionRequest txSubmissionRequest, MsgAcceptTx msgAcceptTx) {
//        System.out.println("TxId : " + txSubmissionRequest.getTxHash());
//      }
//      @Override
//      public void txRejected(TxSubmissionRequest txSubmissionRequest, MsgRejectTx msgRejectTx) {
//        String reasonCbor = msgRejectTx.getReasonCbor();
//        DataItem[] dataItem = CborSerializationUtil.deserialize(
//            HexUtil.decodeHexString(reasonCbor));
//        System.out.println("Rejected: " + reasonCbor);
//      }
//    });
//
//    localClientProvider.start();
//    return localClientProvider;
//  }
//
//  @Bean
//  @DependsOn("localClientProvider")
//  LocalStateQueryClient localStateQueryClient(@Autowired LocalClientProvider localClientProvider){
//    return localClientProvider.getLocalStateQueryClient();
//  }
//
//  @Bean
//  @DependsOn("localClientProvider")
//  LocalTxSubmissionClient localTxSubmissionClient(@Autowired LocalClientProvider localClientProvider){
//    return localClientProvider.getTxSubmissionClient();
//  }
//}
