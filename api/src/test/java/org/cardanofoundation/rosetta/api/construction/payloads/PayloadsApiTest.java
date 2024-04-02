package org.cardanofoundation.rosetta.api.construction.payloads;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.ConstructionPayloadsRequest;
import org.openapitools.client.model.ConstructionPayloadsResponse;
import org.springframework.beans.factory.annotation.Autowired;


public class PayloadsApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  private ConstructionPayloadsRequest getPayloadRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionPayloadsRequest request = mapper.readValue(file, ConstructionPayloadsRequest.class);
    return request;
  }

  @Test
  public void payloadsMultipleInputTests() throws Exception {
    ConstructionPayloadsRequest request = getPayloadRequest("testdata/construction/payloads/multiple_inputs.json");

    ConstructionPayloadsResponse body = constructionApiService.constructionPayloadsService(
        request);
    String expectedUnsignedTransaction = "8279013661343030383238323538323032663233666438636361383335616632316633616333373562616336303166393765616437356632653739313433626466373166653263346265303433653866303138323538323032663233666438636361383335616632316633616333373562616336303166393765616437356632653739313433626466373166653263346265303433653866303130313832383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139323731303832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666231393963343030323139633335303033313930336538a16a6f7065726174696f6e7382a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565662d39303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74667374617475736773756363657373647479706565696e707574a6746f7065726174696f6e5f6964656e746966696572a265696e646578016d6e6574776f726b5f696e64657800676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565662d31303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74667374617475736773756363657373647479706565696e707574";
    assertEquals(expectedUnsignedTransaction, body.getUnsignedTransaction());
  }

  @Test
  public void byronInputTest() throws Exception {
    ConstructionPayloadsRequest request = getPayloadRequest("testdata/construction/payloads/byronInput.json");

    ConstructionPayloadsResponse body = constructionApiService.constructionPayloadsService(
        request);
    String expectedUnsignedTransaction = "8278ee61343030383138323538323032663233666438636361383335616632316633616333373562616336303166393765616437356632653739313433626466373166653263346265303433653866303130313832383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139323731303832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666231393963343030323139396334303033313930336538a16a6f7065726174696f6e7381a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800676163636f756e74a16761646472657373783b416532746450775550455a4336574a66565178544e4e3274577734736b47724e367a5256756b76784a6d544679316e596b5647514275555255334c66616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565662d39303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74667374617475736773756363657373647479706565696e707574";
    assertEquals(expectedUnsignedTransaction, body.getUnsignedTransaction());
  }

  @Test
  public void depositTest() throws Exception {
    ConstructionPayloadsRequest request = getPayloadRequest("testdata/construction/payloads/deposit.json");

    ConstructionPayloadsResponse body = constructionApiService.constructionPayloadsService(
        request);
    String expectedUnsignedTransaction = "8279013a6135303038313832353832303266323366643863636138333561663231663361633337356261633630316639376561643735663265373931343362646637316665326334626530343365386630313031383238323538316436316262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662313932373130383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139396334303032316130303661306337303033313930336538303438313832303038323030353831636262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662a16a6f7065726174696f6e7382a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565682d393030303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74667374617475736773756363657373647479706565696e707574a5746f7065726174696f6e5f6964656e746966696572a165696e64657803676163636f756e74a16761646472657373783b7374616b653175786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e376361656b376135686d65746164617461a2727374616b696e675f63726564656e7469616ca2696865785f62797465737840314234303044363041414633344541463644434241423942424134363030314132333439373838364346313130363646373834363933334433304535414433466a63757276655f747970656c6564776172647332353531396d6465706f736974416d6f756e74a166616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c756567323030303030306673746174757367737563636573736474797065747374616b654b6579526567697374726174696f6e";
    assertEquals(expectedUnsignedTransaction, body.getUnsignedTransaction());
  }

  @Test
  public void simpeInputOutputTest() throws Exception {
    ConstructionPayloadsRequest request = getPayloadRequest("testdata/construction/payloads/simpleInputOutput.json");

    ConstructionPayloadsResponse body = constructionApiService.constructionPayloadsService(
        request);
    String expectedUnsignedTransaction = "8278ee61343030383138323538323032663233666438636361383335616632316633616333373562616336303166393765616437356632653739313433626466373166653263346265303433653866303130313832383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139323731303832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666231393963343030323139396334303033313930336538a16a6f7065726174696f6e7381a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565662d39303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74667374617475736773756363657373647479706565696e707574";
    assertEquals(expectedUnsignedTransaction, body.getUnsignedTransaction());
  }

  @Test
  public void stakeKeyDeregistrationTest() throws Exception {
    ConstructionPayloadsRequest request = getPayloadRequest("testdata/construction/payloads/stakeKey_deregistration.json");

    ConstructionPayloadsResponse body = constructionApiService.constructionPayloadsService(
        request);
    String expectedUnsignedTransaction = "8279013a6135303038313832353832303266323366643863636138333561663231663361633337356261633630316639376561643735663265373931343362646637316665326334626530343365386630313031383238323538316436316262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662313932373130383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139396334303032316130303166323063303033313930336538303438313832303138323030353831636262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662a16a6f7065726174696f6e7382a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565662d39303030306b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74667374617475736773756363657373647479706565696e707574a5746f7065726174696f6e5f6964656e746966696572a165696e64657803676163636f756e74a16761646472657373783b7374616b653175786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e376361656b376135686d65746164617461a1727374616b696e675f63726564656e7469616ca2696865785f62797465737840314234303044363041414633344541463644434241423942424134363030314132333439373838364346313130363646373834363933334433304535414433466a63757276655f747970656c6564776172647332353531396673746174757367737563636573736474797065767374616b654b65794465726567697374726174696f6e";
    assertEquals(expectedUnsignedTransaction, body.getUnsignedTransaction());
  }

}
