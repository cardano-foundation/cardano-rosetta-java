package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import org.cardanofoundation.rosetta.api.model.SubNetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionSubmitRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.impl.ConstructionApiServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ConstructionSubmitUnitTest {

  @Mock
  CardanoService cardanoService;

  @Mock
  LocalTxSubmissionClient localTxSubmissionClient;

  @Mock
  RedisTemplate<String,String> redisTemplate;

  @Mock
  private ValueOperations<String , String> valueOperations;
  @InjectMocks
  ConstructionApiServiceImpl service;



  @Test
  void testConstructionSubmitService_LocalTxSubmissionClientReturnsSuccess() throws Exception {
    // Setup
    final SubNetworkIdentifier subNetworkIdentifier = new SubNetworkIdentifier();
    final ConstructionSubmitRequest constructionSubmitRequest = new ConstructionSubmitRequest(
        new NetworkIdentifier("cardano", "testnet", subNetworkIdentifier), "8279024438346134303038313832353832306231633061356262316335313162366237306563636536373930663130306635343631633934643435356534353536383233316335326435333535316131353230303031383238323538333930303037343364313663666533633466636330633131633234303362626331306462633765636464343437376530353334383161333638653761303665326165343464666636373730646330663461646133636634636632363035303038653237616563646233333261643334396664613731613030393839363830383235383339303066396161626265323939653863663237333834336636323863376637376264646634323030623037313832313732653964333339303236366462313765653763346231333739363238393235303934346636646561663363646530366430653266333832366565343862636432613562316130303562386438303032316130303364303930303033316130316431383434616131303038313832353832306331313764396464383734343437663437333036663530613635306631653038626634626563326366636232616639313636306632336632646239313239373735383430393236303738323430393437663535306437623131373661383363336638336666653465346533383966333963393635303261346635643330633063616530306435663164356437363462663237306235303935373034306230646330356337623239646164363563353864633265326630636439666535393262653137306166356636a16a6f7065726174696f6e7380");
    Array array = (Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
        HexUtil.decodeHexString(constructionSubmitRequest.getSignedTransaction()));
    byte[] signedTransactionBytes = HexUtil.decodeHexString(((UnicodeString) array.getDataItems().get(0)).getString());
    // Configure LocalTxSubmissionClient.submitTx(...).
    final Mono<TxResult> resultMono = Mono.just(new TxResult("f061e62c43e6902169a84f3344a329720cdbf8d75ae3a2845c11249c3e56ce1c",true,null));
    when(cardanoService.decodeExtraData(anyString())).thenReturn(array);
    when(localTxSubmissionClient.submitTx(any(TxSubmissionRequest.class))).thenReturn(resultMono);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    doNothing().when(valueOperations).set(anyString(), anyString(), any());
    TransactionIdentifierResponse transactionIdentifierResponse=service.constructionSubmitService(constructionSubmitRequest);
    assertEquals("f061e62c43e6902169a84f3344a329720cdbf8d75ae3a2845c11249c3e56ce1c",transactionIdentifierResponse.getTransactionIdentifier().getHash());
  }
}
