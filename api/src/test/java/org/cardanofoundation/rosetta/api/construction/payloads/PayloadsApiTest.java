package org.cardanofoundation.rosetta.api.construction.payloads;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.ConstructionPayloadsRequest;
import org.openapitools.client.model.ConstructionPayloadsResponse;



public class PayloadsApiTest {

  private ConstructionApiService constructionApiService;

  private ConstructionPayloadsRequest getPayloadRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionPayloadsRequest request = mapper.readValue(file, ConstructionPayloadsRequest.class);
    return request;
  }

  @Test
  public void payloadsMultipleInputTests() throws Exception {
    ConstructionPayloadsRequest request = getPayloadRequest("testdata/construction/payloads/multipleInputPayloads.json");

    ConstructionPayloadsResponse body = constructionApiService.constructionPayloadsService(
        request);
    String expectedUnsignedTransaction = "8279013661343030383238323538323032663233666438636361383335616632316633616333373562616336303166393765616437356632653739313433626466373166653263346265303433653866303138323538323032663233666438636361383335616632316633616333373562616336303166393765616437356632653739313433626466373166653263346265303433653866303130313832383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139323731303832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666231393963343030323139633335303033313930336538a16a6f7065726174696f6e7382a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565662d39303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74a6746f7065726174696f6e5f6964656e746966696572a265696e646578016d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565662d31303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74";
    assertEquals(expectedUnsignedTransaction, body.getUnsignedTransaction());
  }

}
