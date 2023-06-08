package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.iwebpp.crypto.TweetNacl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.Amount;
import org.cardanofoundation.rosetta.api.model.CoinChange;
import org.cardanofoundation.rosetta.api.model.CoinIdentifier;
import org.cardanofoundation.rosetta.api.model.ConstructionMetadataRequestOptions;
import org.cardanofoundation.rosetta.api.model.ConstructionPayloadsRequestMetadata;
import org.cardanofoundation.rosetta.api.model.ConstructionPreprocessRequestMetadata;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.DepositParameters;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.OperationIdentifier;
import org.cardanofoundation.rosetta.api.model.OperationMetadata;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.Signature;
import org.cardanofoundation.rosetta.api.model.SignatureType;
import org.cardanofoundation.rosetta.api.model.SigningPayload;
import org.cardanofoundation.rosetta.api.model.SubNetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsResponse;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionCombineRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionCombineResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionHashRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionSubmitRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.impl.ConstructionApiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;

@Slf4j
public class ConstructionApiDelegateImplSubmitTests extends IntegrationTest {

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/");
  }
  private final String BASE_DIRECTORY = "src/test/resources/files/construction/submit";
  @Test
  void test_should_fail_if_request_is_not_valid() throws IOException {
    ConstructionHashRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_submit_failed.json"))),
        ConstructionHashRequest.class);
    try {
    restTemplate.postForObject(
        baseUrl+"construction/submit", request, TransactionIdentifierResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error=objectMapper.readValue(responseBody,Error.class);
      assertTrue(!error.isRetriable());
      assertEquals(5019,error.getCode());
      assertEquals("The transaction submission has been rejected",error.getMessage());
    }

  }


//  @Test
//  void test_should_return_the_transaction_identifier_if_request_is_valid() throws IOException {
//    String PRIVATE_KEY =
//        "41d9523b87b9bd89a4d07c9b957ae68a7472d8145d7956a692df1a8ad91957a2c117d9dd874447f47306f50a650f1e08bf4bec2cfcb2af91660f23f2db912977";
//    String SEND_FUNDS_ADDRESS =
//        "addr1qqr585tvlc7ylnqvz8pyqwauzrdu0mxag3m7q56grgmgu7sxu2hyfhlkwuxupa9d5085eunq2qywy7hvmvej456flknsug829n";
//    TweetNacl.Signature.KeyPair signature=TweetNacl.Signature.keyPair_fromSecretKey(HexUtil.decodeHexString(PRIVATE_KEY));
//    NetworkIdentifier networkIdentifier=new NetworkIdentifier("cardano","testnet",null);
//    ConstructionDeriveRequest deriveRequest =
//        new ConstructionDeriveRequest(
//                                      networkIdentifier,
//                                      new PublicKey(
//                                      HexUtil.encodeHexString(signature.getPublicKey()),
//                                      "edwards25519"
//                                      ),
//                                      null
//    );
//
//    ConstructionDeriveResponse constructionDeriveResponse =restTemplate.postForObject(
//          baseUrl+"construction/derive", deriveRequest, ConstructionDeriveResponse.class);
//    String address=constructionDeriveResponse.getAccountIdentifier().getAddress();
//
//    AccountCoinsRequest accountCoinsRequest =
//        new AccountCoinsRequest(
//            networkIdentifier
//            ,new AccountIdentifier(address,null,null),false,
//            null
//        );
//
//    AccountCoinsResponse accountCoinsResponse =restTemplate.postForObject(
//        baseUrl+"account/coins", accountCoinsRequest, AccountCoinsResponse.class);
//
//    AccountBalanceRequest accountBalanceRequest =
//        new AccountBalanceRequest(
//            networkIdentifier
//            ,new AccountIdentifier(address,null,null),null,
//            null
//        );
//
//    AccountBalanceResponse accountBalanceResponse =restTemplate.postForObject(
//        baseUrl+"account/balance", accountBalanceRequest, AccountBalanceResponse.class);
//
//  List<Operation> builtOperations = buildOperation(
//        accountCoinsResponse,
//        accountBalanceResponse,
//        address,
//        SEND_FUNDS_ADDRESS,
//      null,
//      null
//    );
//
// ConstructionMetadataRequestOptions preprocess = constructionPreprocess(networkIdentifier,
//        builtOperations,
//        1000.0,
//     null
//    );
// ConstructionPayloadsRequestMetadata metadata =constructionMetadata(networkIdentifier,preprocess);
//  ConstructionPayloadsResponse payloads = constructionPayloads(networkIdentifier,
//        builtOperations,
//        metadata
//  );
//    List<Signature> signatures = signPayloads(payloads.getPayloads(),signature);
//  ConstructionCombineResponse combined = constructionCombine(networkIdentifier,
//        payloads.getUnsignedTransaction(),
//        signatures
//    );
//    log.info("[doRun] signed transaction is ${combined.signed_transaction}");
//    ConstructionSubmitRequest request = new ConstructionSubmitRequest(networkIdentifier,combined.getSignedTransaction());
//    try {
//      TransactionIdentifierResponse transactionIdentifierResponse=restTemplate.postForObject(
//          baseUrl+"construction/submit", request, TransactionIdentifierResponse.class);
//      log.info("Transaction is submitted successfully ,having the hash :" +transactionIdentifierResponse.getTransactionIdentifier().getHash());
//
//    } catch (HttpServerErrorException e) {
//      String responseBody = e.getResponseBodyAsString();
//      Error error=objectMapper.readValue(responseBody,Error.class);
//      assertTrue(!error.isRetriable());
//      assertEquals(5019,error.getCode());
//      assertEquals("The transaction submission has been rejected",error.getMessage());
//    }
//    log.info(
//        "[doRun] transaction with hash ${hashResponse.transaction_identifier.hash} sent"
//  );
//
//  }
public List<Operation> buildOperation(AccountCoinsResponse unspents,
                                      AccountBalanceResponse balances,
                                      String address,
                                      String destination,
                                      Boolean isRegisteringStakeKey,
                                      Integer outputsPerc
                                      ){
  if(isRegisteringStakeKey==null) isRegisteringStakeKey = false;
  if(outputsPerc==null) outputsPerc = 95;
  List<TokenBundleItem> tokenBundle = new ArrayList<>();
  AtomicLong index= new AtomicLong();
  List<Operation> inputs = unspents.getCoins().stream().map(coin->{
    Operation operation=new Operation(
        new OperationIdentifier(index.getAndIncrement(),0l),
        null,
        "input",
        "success",
        new AccountIdentifier(address,null,null),
        new Amount(coin.getAmount().getValue(),new Currency(coin.getAmount().getCurrency().getSymbol(),coin.getAmount().getCurrency().getDecimals(),null)),
        new CoinChange(new CoinIdentifier(coin.getCoinIdentifier().getIdentifier()),"coin_created"),
        null
    );
    if (coin.getMetadata()!=null) {
      Set<String> coinsWithMa = coin.getMetadata().keySet();
      List<TokenBundleItem> tokenBundleList=new ArrayList<>();
      coinsWithMa.stream().forEach(coinWithMa-> {
          List<TokenBundleItem> tokenBundleItems = coin.getMetadata().get(coinWithMa);
        tokenBundleList.addAll(tokenBundleItems);
      }
      );
      List<TokenBundleItem> coinTokenBundleItems=tokenBundleList;
      tokenBundle.addAll(coinTokenBundleItems);
      List<org.cardanofoundation.rosetta.api.model.TokenBundleItem> list=new ArrayList<>();
      tokenBundle.stream().forEach(t->{
        try {
          String tokenBundleString=objectMapper.writeValueAsString(t);
          org.cardanofoundation.rosetta.api.model.TokenBundleItem tokenBundleItem=objectMapper.readValue(tokenBundleString,
              org.cardanofoundation.rosetta.api.model.TokenBundleItem.class);
          list.add(tokenBundleItem);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });
      OperationMetadata operationMetadata=new OperationMetadata(list);
      operation.setMetadata(operationMetadata);
    }
    operation.getAmount().setValue("-"+operation.getAmount().getValue());
    return operation;
  }).collect(Collectors.toList());
  // TODO: No proper fees estimation is being done (it should be transaction size based)
  Long adaBalance = Long.parseLong(balances.getBalances().get(0).getValue());
  Long outputAmount = (adaBalance * outputsPerc) / 100;
  if (isRegisteringStakeKey) {
    int i = 0;
    do {
      long dividend = outputsPerc - i;
      outputAmount = (adaBalance * dividend) / 100;
      i += 5;
      if (outputAmount < 2500000)
        throw ExceptionFactory.outPutTooLow();
    } while (adaBalance - outputAmount <= 2000000);
  }

  Operation outputOp=new Operation(
      new OperationIdentifier((long) inputs.size(),0l),
      null,
      "output",
      "success",
      new AccountIdentifier(destination,null,null),
      new Amount(outputAmount.toString(),new Currency("ADA",6,null)),
      null,
      null
  );
  List<org.cardanofoundation.rosetta.api.model.TokenBundleItem> list=new ArrayList<>();
  tokenBundle.stream().forEach(t->{
    try {
      String tokenBundleString=objectMapper.writeValueAsString(t);
      org.cardanofoundation.rosetta.api.model.TokenBundleItem tokenBundleItem=objectMapper.readValue(tokenBundleString,
          org.cardanofoundation.rosetta.api.model.TokenBundleItem.class);
      list.add(tokenBundleItem);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  });
  OperationMetadata operationMetadata=new OperationMetadata(list);
  if (tokenBundle.size() > 0) outputOp.setMetadata(operationMetadata);
  inputs.add(outputOp);
  return inputs;
}
  public ConstructionMetadataRequestOptions constructionPreprocess(NetworkIdentifier networkIdentifier,
    List operations,
    double relative_ttl,
    DepositParameters depositParameters
    ) throws JsonProcessingException {
        if(depositParameters==null) depositParameters=new DepositParameters("2000000","500000000");
    ConstructionPreprocessRequest request =
        new ConstructionPreprocessRequest(networkIdentifier,
                                          operations,
                                          new ConstructionPreprocessRequestMetadata(relative_ttl,depositParameters),
                                   null,
                        null);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl+"construction/preprocess", request, ConstructionPreprocessResponse.class);
    String options= objectMapper.writeValueAsString(constructionPreprocessResponse.getOptions());
    return objectMapper.readValue(options,ConstructionMetadataRequestOptions.class);
      }

  public ConstructionPayloadsRequestMetadata constructionMetadata(NetworkIdentifier networkIdentifier,ConstructionMetadataRequestOptions options)
      throws JsonProcessingException {
    ConstructionMetadataRequest request = new ConstructionMetadataRequest(networkIdentifier,options,null);

    ConstructionMetadataResponse response = restTemplate.postForObject(baseUrl+"construction/metadata",
        request, ConstructionMetadataResponse.class);
    String metadata= objectMapper.writeValueAsString(response.getMetadata());
    return objectMapper.readValue(metadata,ConstructionPayloadsRequestMetadata.class);
  }

  public ConstructionPayloadsResponse constructionPayloads(NetworkIdentifier networkIdentifier,
                                                            List<Operation> operations,
                                                            ConstructionPayloadsRequestMetadata metadata){
    ConstructionPayloadsRequest request = new ConstructionPayloadsRequest(
                                                                          networkIdentifier,
                                                                          operations,
                                                                          metadata,
                                                                          null
    );
    return restTemplate.postForObject(baseUrl+"construction/payloads",
        request, ConstructionPayloadsResponse.class);
  }
  public List<Signature>  signPayloads(List<SigningPayload> payloads,TweetNacl.Signature.KeyPair keyPair){
   return payloads.stream().map(signing_payload->{
     TweetNacl.Signature signature=new TweetNacl.Signature(null,keyPair.getSecretKey());
     byte[] result= signature.detached(HexUtil.decodeHexString(signing_payload.getHexBytes()));
     String string=HexUtil.encodeHexString(result);
      return new Signature(
          signing_payload,
          new PublicKey(HexUtil.encodeHexString(keyPair.getPublicKey()),"edwards25519"),
          SignatureType.ED25519,
          HexUtil.encodeHexString(result)
          );
    }).collect(Collectors.toList());
  }
  public ConstructionCombineResponse constructionCombine(NetworkIdentifier networkIdentifier,
                                                         String unsignedTransaction,
                                                          List<Signature> signatures)
      throws IOException {
    ConstructionCombineRequest request = new ConstructionCombineRequest(networkIdentifier,unsignedTransaction,signatures);

    return restTemplate.postForObject(
        baseUrl+"construction/combine", request, ConstructionCombineResponse.class);
  }


}
