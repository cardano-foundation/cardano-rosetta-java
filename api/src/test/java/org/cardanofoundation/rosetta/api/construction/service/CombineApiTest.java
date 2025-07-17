package org.cardanofoundation.rosetta.api.construction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openapitools.client.model.ConstructionCombineRequest;
import org.openapitools.client.model.ConstructionCombineResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.bloxbean.cardano.client.crypto.Blake2bUtil.blake2bHash256;
import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;
import static com.bloxbean.cardano.client.util.HexUtil.encodeHexString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class CombineApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  @Test
  void combineWithByronAddressTest() throws IOException {
    ConstructionCombineRequest combineRequest = getCombineRequest(
        "testdata/construction/combine/combine_with_byron_addresses.json");

    ConstructionCombineResponse constructionCombineResponse =
        constructionApiService.constructionCombineService(combineRequest);

    System.out.println(constructionCombineResponse.getSignedTransaction());

    String signedTransaction = "827902183834613430306439303130323831383235383230326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663031303138323832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666231393237313038323538316436316262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662313939633430303231393963343030333139303365386131303264393031303238313834353832303733666561383064343234323736616430393738643466653533313065386263326434383566356636626233626638373631323938396631313261643561376435383430646332613139343862666139343131623337653864323830623034633438613835616635353838626366353039633066636137393866376234363265626361393264363733336461636331663163366331343633363233633038353430316265303765613432326164346631633534333337356537643364323339336161306235383230646437356531353464613431376265636563353563646432343933323734353431333866303832313130323937643565383761623235653135666164313530663431613066356636a16a6f7065726174696f6e7381a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800676163636f756e74a16761646472657373783b416532746450775550455a4336574a66565178544e4e3274577734736b47724e367a5256756b76784a6d544679316e596b5647514275555255334c66616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565662d39303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74667374617475736773756363657373647479706565696e707574";

    assertEquals(encodeHexString(blake2bHash256(decodeHexString(signedTransaction))), encodeHexString(blake2bHash256(decodeHexString(constructionCombineResponse.getSignedTransaction()))));
  }

  @Test
  void combineWithInvalidSignatureTest() throws IOException {
    ConstructionCombineRequest combineRequest = getCombineRequest(
        "testdata/construction/combine/combine_with_invalid_transaction.json");

    ApiException actualException = assertThrows(ApiException.class, () ->
        constructionApiService.constructionCombineService(combineRequest));

    assertEquals(5019, actualException.getError().getCode());
  }

  @RepeatedTest(10)
  void combineWithByronAddressMissingChaincodeTest() throws IOException {
    ConstructionCombineRequest combineRequest = getCombineRequest(
        "testdata/construction/combine/combine_with_byron_addresses_missing_chaincode.json");

    ApiException actualException = assertThrows(ApiException.class, () ->
        constructionApiService.constructionCombineService(combineRequest));

    assertEquals(RosettaErrorType.CHAIN_CODE_MISSING.getCode(), actualException.getError().getCode());
    assertEquals(RosettaErrorType.CHAIN_CODE_MISSING.getMessage(), actualException.getError().getMessage());
  }

  private ConstructionCombineRequest getCombineRequest(String fileName) throws IOException {
    File file = new File(
        Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName)).getFile());
    ObjectMapper mapper = new ObjectMapper();

    return mapper.readValue(file, ConstructionCombineRequest.class);
  }

}
