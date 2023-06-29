package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.iwebpp.crypto.TweetNacl;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.rest.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
      Error error=objectMapper.readValue(responseBody, Error.class);
      assertTrue(error.isRetriable());
      assertEquals(5000,error.getCode());
      assertEquals("An error occurred",error.getMessage());
    }

  }
//  @Test
//  void with_ma_test_should_return_the_transaction_identifier_if_request_is_valid() throws IOException {
//    String PAYMENT_ADDRESS="addr_test1vpcv26kdu8hr9x939zktp275xhwz4478c8hcdt7l8wrl0ecjftnfa";
//    String EXPECTED_TOKEN_policy="3e6fc736d30770b830db70994f25111c18987f1407585c0f55ca470f";
//    String EXPECTED_TOKEN_symbol="6a78546f6b656e31";
//    String PAYMENT_KEYS_secretKey="67b638cef68135c4005cb71782b070c4805c9e1077c7ab6145b152206073272974dabdc594506574a9b58f719787d36ea1af291d141d3e5e5ccfe076909ae106";
//    byte[] PAYMENT_KEYS_publicKey=HexUtil.decodeHexString("74dabdc594506574a9b58f719787d36ea1af291d141d3e5e5ccfe076909ae106");
//    String SEND_FUNDS_ADDRESS="addr_test1qr3cuxr0qy624z98sfx587gh64wsejjx2ryzpdhaf8cxs96c2gpzd7eql8syzasdyzcdy9lgc68kprar0g49c9f95y7q0wfhfp";
//    submit(true,PAYMENT_KEYS_secretKey,SEND_FUNDS_ADDRESS,PAYMENT_ADDRESS);
//  }

//  @Test
//  void test_should_return_the_transaction_identifier_if_request_is_valid() throws IOException {
//    String PRIVATE_KEY =
//        "41d9523b87b9bd89a4d07c9b957ae68a7472d8145d7956a692df1a8ad91957a2c117d9dd874447f47306f50a650f1e08bf4bec2cfcb2af91660f23f2db912977";
//    String SEND_FUNDS_ADDRESS =
//        "addr1qqr585tvlc7ylnqvz8pyqwauzrdu0mxag3m7q56grgmgu7sxu2hyfhlkwuxupa9d5085eunq2qywy7hvmvej456flknsug829n";
//    submit(false,PRIVATE_KEY,SEND_FUNDS_ADDRESS,null);
//  }

  public void submit(Boolean MA,String PRIVATE_KEY,String SEND_FUNDS_ADDRESS,String PAYMENT_ADDRESS) throws IOException {
    TweetNacl.Signature.KeyPair signature=TweetNacl.Signature.keyPair_fromSecretKey(HexUtil.decodeHexString(PRIVATE_KEY));
    NetworkIdentifier networkIdentifier=new NetworkIdentifier("cardano","testnet",null);
    String address=null;
    if(!MA){
      ConstructionDeriveRequest deriveRequest =
          new ConstructionDeriveRequest(
              networkIdentifier,
              new PublicKey(
                  HexUtil.encodeHexString(signature.getPublicKey()),
                  "edwards25519"
              ),
              null
          );

      ConstructionDeriveResponse constructionDeriveResponse = restTemplate.postForObject(
          baseUrl + "construction/derive", deriveRequest, ConstructionDeriveResponse.class);
      address = constructionDeriveResponse.getAccountIdentifier().getAddress();
    }else{
      address=PAYMENT_ADDRESS;
    }

    AccountCoinsRequest accountCoinsRequest =
        new AccountCoinsRequest(
            networkIdentifier
            ,new AccountIdentifier(address,null,null),false,
            null
        );

    AccountCoinsResponse accountCoinsResponse =restTemplate.postForObject(
        baseUrl+"account/coins", accountCoinsRequest, AccountCoinsResponse.class);

    AccountBalanceRequest accountBalanceRequest =
        new AccountBalanceRequest(
            networkIdentifier
            ,new AccountIdentifier(address,null,null),null,
            null
        );

    AccountBalanceResponse accountBalanceResponse =restTemplate.postForObject(
        baseUrl+"account/balance", accountBalanceRequest, AccountBalanceResponse.class);

    List<Operation> builtOperations = buildOperation(
        accountCoinsResponse,
        accountBalanceResponse,
        address,
        SEND_FUNDS_ADDRESS,
        null,
        null
    );
    String operation=objectMapper.writeValueAsString(builtOperations);
    ConstructionMetadataRequestOptions preprocess = constructionPreprocess(networkIdentifier,
        builtOperations,
        1000.0,
        null
    );
    ConstructionPayloadsRequestMetadata metadata =constructionMetadata(networkIdentifier,preprocess);
    ConstructionPayloadsResponse payloads = constructionPayloads(networkIdentifier,
        builtOperations,
        metadata
    );
    List<Signature> signatures = signPayloads(payloads.getPayloads(),signature);
    ConstructionCombineResponse combined = constructionCombine(networkIdentifier,
        payloads.getUnsignedTransaction(),
        signatures
    );
    log.info("[doRun] signed transaction is ${combined.signed_transaction}");
    System.out.println("combined.getSignedTransaction()  "+combined.getSignedTransaction());
    ConstructionSubmitRequest request = new ConstructionSubmitRequest(networkIdentifier,combined.getSignedTransaction());
    try {
      TransactionIdentifierResponse transactionIdentifierResponse=restTemplate.postForObject(
          baseUrl+"construction/submit", request, TransactionIdentifierResponse.class);
      log.info("Transaction is submitted successfully ,having the hash :" +transactionIdentifierResponse.getTransactionIdentifier().getHash());

    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error=objectMapper.readValue(responseBody, Error.class);
      assertTrue(!error.isRetriable());
      assertEquals(5019,error.getCode());
      assertEquals("The transaction submission has been rejected",error.getMessage());
    }
    log.info(
        "[doRun] transaction with hash ${hashResponse.transaction_identifier.hash} sent"
    );
  }
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
        tokenBundleList.stream().forEach(t->{
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
    ArrayList<String> checkSymBolList=new ArrayList<>();
    List<org.cardanofoundation.rosetta.api.model.TokenBundleItem> newList=new ArrayList<>();

    list.stream().forEach(l->{
      if(!checkSymBolList.contains(l.getPolicyId())){
        newList.add(new org.cardanofoundation.rosetta.api.model.TokenBundleItem(l.getPolicyId()
            , new ArrayList<>(List.of(new Amount("0",
            new Currency(l.getTokens().get(0).getCurrency().getSymbol(),
                l.getTokens().get(0).getCurrency().getDecimals(),
                new Metadata(l.getTokens().get(0).getCurrency().getMetadata().getPolicyId())))))));
      }
      checkSymBolList.add(l.getPolicyId());
    });
    list.stream().forEach(l->{
      newList.stream().forEach(n->{
        if(n.getPolicyId().equals(l.getPolicyId())){
          n.getTokens().get(0).setValue((Long.parseLong(n.getTokens().get(0).getValue())+
              Long.parseLong(l.getTokens().get(0).getValue()))+"");
        }
      });
    });
    OperationMetadata operationMetadata2=new OperationMetadata(newList);
    if (tokenBundle.size() > 0) outputOp.setMetadata(operationMetadata2);
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
          string
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
