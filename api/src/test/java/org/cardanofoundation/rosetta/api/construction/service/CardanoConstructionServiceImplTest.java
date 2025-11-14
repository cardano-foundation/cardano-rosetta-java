package org.cardanofoundation.rosetta.api.construction.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.exception.CborRuntimeException;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import jakarta.validation.constraints.NotNull;
import org.cardanofoundation.rosetta.api.construction.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.Error;
import org.cardanofoundation.rosetta.common.mapper.CborArrayToTransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionParsed;
import org.cardanofoundation.rosetta.common.time.OfflineSlotServiceImpl;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.PublicKey;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.bloxbean.cardano.client.crypto.Blake2bUtil.blake2bHash256;
import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;
import static com.bloxbean.cardano.client.util.HexUtil.encodeHexString;
import static org.cardanofoundation.rosetta.EntityGenerator.*;
import static org.cardanofoundation.rosetta.api.construction.enumeration.AddressType.BASE;
import static org.cardanofoundation.rosetta.api.construction.enumeration.AddressType.REWARD;
import static org.cardanofoundation.rosetta.common.enumeration.NetworkEnum.PREPROD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openapitools.client.model.CurveType.EDWARDS25519;

@ExtendWith(MockitoExtension.class)
class CardanoConstructionServiceImplTest {

  private final static String TRANSACTION_SIGNED = "8279025a3833613430303831383235383230326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663031303138323832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666238323139323731306131353831636230643037643435666539353134663830323133663430323065356136313234313435386265363236383431636465373137636233386137613334393437373536393634366634333666363936653139303930363530346137353631366534333732373537613534366636623635366536313761366631393161306134373665373537343633366636393665313932373130383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139396334303032313939633430303330306131303038313832353832303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303035383430303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303066356636a16a6f7065726174696f6e7381a7746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26576616c7565662d39303030306863757272656e6379a26673796d626f6c6341444168646563696d616c73066b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74686d65746164617461a16b746f6b656e42756e646c6581a268706f6c69637949647838623064303764343566653935313466383032313366343032306535613631323431343538626536323638343163646537313763623338613766746f6b656e7383a26576616c756564323331306863757272656e6379a26673796d626f6c7234373735363936343666343336663639366568646563696d616c7300a26576616c756564363636366863757272656e6379a26673796d626f6c7820346137353631366534333732373537613534366636623635366536313761366668646563696d616c7300a26576616c75656531303030306863757272656e6379a26673796d626f6c6e366537353734363336663639366568646563696d616c7300";
  private final static String TRANSACTION_NOT_SIGNED = "82790132613530303831383235383230326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663031303138323832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666230313832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666230343032316130353762636566623033313930336538303438313832303138323030353831636262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662a16a6f7065726174696f6e7382a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26576616c7565692d39303030303030306863757272656e6379a26673796d626f6c6341444168646563696d616c73066b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74a5746f7065726174696f6e5f6964656e746966696572a165696e646578036474797065767374616b654b65794465726567697374726174696f6e667374617475736773756363657373676163636f756e74a16761646472657373783b7374616b653175387a666e6b687034673676686e6565746d763271656e3766356e64726b6c716a7138653973326e636b3968333063667a36716d70686d65746164617461a2727374616b696e675f63726564656e7469616ca2696865785f62797465737840314234303044363041414633344541463644434241423942424134363030314132333439373838364346313130363646373834363933334433304535414433466a63757276655f747970656c6564776172647332353531396c726566756e64416d6f756e74a26576616c7565682d323030303030306863757272656e6379a26673796d626f6c6341444168646563696d616c7306";
  private final static String COMBINE_UNSIGNED_TRANSACTION = "a400818258202f23fd8cca835af21f3ac375bac601f97ead75f2e79143bdf71fe2c4be043e8f01018282581d61bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb19271082581d61bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb199c4002199c40031903e8";
  private final static String COMBINE_SIGNED_TRANSACTION = "84a400d90102818258202f23fd8cca835af21f3ac375bac601f97ead75f2e79143bdf71fe2c4be043e8f01018282581d61bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb19271082581d61bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb199c4002199c40031903e8a102d901028184582073fea80d424276ad0978d4fe5310e8bc2d485f5f6bb3bf87612989f112ad5a7d5840dc2a1948bfa9411b37e8d280b04c48a85af5588bcf509c0fca798f7b462ebca92d6733dacc1f1c6c1463623c085401be07ea422ad4f1c543375e7d3d2393aa0b5820dd75e154da417becec55cdd249327454138f082110297d5e87ab25e15fad150f41a0f5f6";

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private CardanoConstructionServiceImpl cardanoService;

  private MockedStatic<CompletableFuture> completableFutureMock;

  @Spy
  private Clock clock = Clock.systemDefaultZone();

  @Spy
  private OfflineSlotServiceImpl offlineSlotService = new OfflineSlotServiceImpl(clock, ZoneOffset.UTC);

  @BeforeEach
  void setup() {
    cardanoService = new CardanoConstructionServiceImpl(null, null,
        new TransactionOperationParserImpl(), restTemplate, offlineSlotService);
    completableFutureMock = Mockito.mockStatic(CompletableFuture.class, invocation -> {
      if (invocation.getMethod().getName().equals("supplyAsync")) {
        Supplier<?> supplier = invocation.getArgument(0);
        return CompletableFuture.completedFuture(supplier.get());
      }
      return invocation.callRealMethod();
    });
  }

  @AfterEach
  void tearDown() {
    completableFutureMock.close();
  }

  @SuppressWarnings("java:S5778")
  @Test
  void calculateTxSize_whenUnsignedTransactionAddressesInvalid_thenThrowException() {
    List<Operation> operations = Collections.singletonList(givenOperation());
    try (MockedStatic<CardanoAddressUtils> mockedAddressUtil = Mockito.mockStatic(
        CardanoAddressUtils.class)) {
      mockedAddressUtil.when(() -> CardanoAddressUtils.getEraAddressType(anyString()))
          .thenReturn(null);
      mockedAddressUtil.when(() -> CardanoAddressUtils.isEd25519KeyHash(anyString()))
          .thenReturn(false);

      ApiException result = assertThrows(ApiException.class,
          () -> cardanoService.calculateTxSize(NetworkEnum.PREVIEW.getNetwork(), operations, 0));

      assertEquals(RosettaErrorType.INVALID_ADDRESS.getMessage(), result.getError().getMessage());
      assertEquals(RosettaErrorType.INVALID_ADDRESS.getCode(), result.getError().getCode());
      assertTrue(result.getError().isRetriable());

      mockedAddressUtil.verify(() -> CardanoAddressUtils.getEraAddressType(anyString()), times(1));
      mockedAddressUtil.verify(() -> CardanoAddressUtils.isEd25519KeyHash(anyString()), times(1));
    }
  }

  @SuppressWarnings("java:S5778")
  @Test
  void convertRosettaOperationsWOOperationType() {
    ApiException exception = assertThrows(ApiException.class,
            () -> cardanoService.convertRosettaOperations(NetworkEnum.MAINNET.getNetwork(), List.of(new Operation())));

    assertEquals(4019, exception.getError().getCode());
    assertEquals("Provided operation type is invalid", exception.getError().getMessage());
  }

  @Test
  void extractTransactionRosettaTxTest() {
    String packedRosettaTransaction = "827901d4383461343030383138323538323037663639306539666234313034383732636136613361353961373636363663633236303737303064323236323839313832643933626564646266393536363138303130313832383235383164363066306233656133363935343261353834383239306237323736343862353833386235646161323535396465626435356466636161316632303161303031653834383038323538316436303564643239333565396262333530383331353062633834326661376661303935623734306335636436363031376532623633313531336532316130353932343937393032316130303032616533643033316130333638313930366131303038313832353832303337363939653262386432333835613031623538393439333638336266326435356635396331646463316637306137303466623936356331613930666633303935383430303935656366376431363731333564653064343831353433306662343037396466383563353865316564633930346363383461323864323361633336393839616631373939653463633662343865383738303131366532383263303365343630353738626337663766353266663663613161616632653138336162643835303166356636a26a6f7065726174696f6e7381a6746f7065726174696f6e5f6964656e746966696572a165696e64657800676163636f756e74a16761646472657373783f616464725f746573743176707761397936376e776534707163347030797939376e6c357a326d7773783965346e717a6c33747676323338637367716b616c6c66616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c7565692d39353634383832326b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842376636393065396662343130343837326361366133613539613736363636636332363037373030643232363238393138326439336265646462663935363631383a316b636f696e5f616374696f6e6a636f696e5f7370656e746673746174757360647479706565696e707574767472616e73616374696f6e4d6574616461746148657860";
    String rawTx = cardanoService.extractTransactionIfNeeded(packedRosettaTransaction);
    String expectedTx = "84a400818258207f690e9fb4104872ca6a3a59a76666cc2607700d226289182d93beddbf95661801018282581d60f0b3ea369542a5848290b727648b5838b5daa2559debd55dfcaa1f201a001e848082581d605dd2935e9bb35083150bc842fa7fa095b740c5cd66017e2b631513e21a05924979021a0002ae3d031a03681906a1008182582037699e2b8d2385a01b589493683bf2d55f59c1ddc1f70a704fb965c1a90ff3095840095ecf7d167135de0d4815430fb4079df85c58e1edc904cc84a28d23ac36989af1799e4cc6b48e8780116e282c03e460578bc7f7f52ff6ca1aaf2e183abd8501f5f6";
    assertEquals(expectedTx, rawTx);
  }

  @Test
  void extractTransactionRawTxTest() {
    String rawTx = "84a400818258207f690e9fb4104872ca6a3a59a76666cc2607700d226289182d93beddbf95661801018282581d60f0b3ea369542a5848290b727648b5838b5daa2559debd55dfcaa1f201a001e848082581d605dd2935e9bb35083150bc842fa7fa095b740c5cd66017e2b631513e21a05924979021a0002ae3d031a03681906a1008182582037699e2b8d2385a01b589493683bf2d55f59c1ddc1f70a704fb965c1a90ff3095840095ecf7d167135de0d4815430fb4079df85c58e1edc904cc84a28d23ac36989af1799e4cc6b48e8780116e282c03e460578bc7f7f52ff6ca1aaf2e183abd8501f5f6";
    String extractedTx = cardanoService.extractTransactionIfNeeded(rawTx);
    assertEquals(rawTx, extractedTx);
  }

  @Test
  void parseTransactionSignedTest() {
    TransactionParsed actual = cardanoService.parseTransaction(NetworkEnum.PREVIEW.getNetwork(), TRANSACTION_SIGNED, true);

    assertFalse(actual.operations().isEmpty());
    assertEquals(3, actual.operations().size());
    actual.operations().stream().filter(op -> op.getType().equals("input")).forEach(op -> {
      assertNotNull(op.getOperationIdentifier());
      assertNotNull(op.getAccount());
      assertNotNull(op.getMetadata());
      assertNull(op.getRelatedOperations());
      assertEquals("success", op.getStatus());
      assertEquals("-90000", op.getAmount().getValue());
      assertEquals("coin_spent", op.getCoinChange().getCoinAction().getValue());
    });
    actual.operations().stream().filter(op -> op.getType().equals("output")).forEach(op -> {
      assertNotNull(op.getOperationIdentifier());
      assertNotNull(op.getRelatedOperations());
      assertNotNull(op.getAccount());
      assertNotNull(op.getAmount());
      assertNull(op.getCoinChange());
      assertEquals("", op.getStatus());
    });
  }

  @SuppressWarnings("java:S5778")
  @Test
  void parseTransactionSignedThrowTest() {
    ApiException actualException = assertThrows(ApiException.class, () ->
        cardanoService.parseTransaction(NetworkEnum.PREVIEW.getNetwork(), TRANSACTION_NOT_SIGNED, true));

    assertEquals(RosettaErrorType.CANT_CREATE_SIGNED_TRANSACTION_ERROR.getMessage(),
        actualException.getError().getMessage());
    assertEquals(RosettaErrorType.CANT_CREATE_SIGNED_TRANSACTION_ERROR.getCode(),
        actualException.getError().getCode());
    assertFalse(actualException.getError().isRetriable());
  }

  @Test
  void parseTransactionNotSignedTest() {
    TransactionParsed actual = cardanoService.parseTransaction(NetworkEnum.PREVIEW.getNetwork(), TRANSACTION_NOT_SIGNED, false);

    assertFalse(actual.operations().isEmpty());
    assertEquals(4, actual.operations().size());
    actual.operations().stream().filter(op -> op.getType().equals("input")).forEach(op -> {
      assertNotNull(op.getOperationIdentifier());
      assertNotNull(op.getAccount());
      assertNull(op.getMetadata());
      assertNull(op.getRelatedOperations());
      assertEquals("success", op.getStatus());
      assertEquals("-90000000", op.getAmount().getValue());
      assertEquals("coin_spent", op.getCoinChange().getCoinAction().getValue());
    });

    actual.operations().stream().filter(op -> op.getType().equals("output")).forEach(op -> {
      assertNotNull(op.getOperationIdentifier());
      assertNotNull(op.getRelatedOperations());
      assertNotNull(op.getAccount());
      assertNotNull(op.getAmount());
      assertNull(op.getMetadata());
      assertNull(op.getCoinChange());
      assertEquals("", op.getStatus());
    });
    actual.operations().stream().filter(op -> op.getType().equals("stakeKeyDeregistration"))
        .forEach(op -> {
          assertNotNull(op.getOperationIdentifier());
          assertNotNull(op.getAccount());
          assertNotNull(op.getMetadata());
          assertNull(op.getRelatedOperations());
          assertNull(op.getAmount());
          assertNull(op.getCoinChange());
          assertEquals("", op.getStatus());
        });
  }

  @SuppressWarnings("java:S5778")
  @Test
  void parseTransactionNotSignedThrowTest() {
    ApiException actualException = assertThrows(ApiException.class, () ->
        cardanoService.parseTransaction(NetworkEnum.PREVIEW.getNetwork(), TRANSACTION_SIGNED, false));

    assertEquals(RosettaErrorType.CANT_CREATE_UNSIGNED_TRANSACTION_ERROR.getMessage(),
        actualException.getError().getMessage());
    assertEquals(RosettaErrorType.CANT_CREATE_UNSIGNED_TRANSACTION_ERROR.getCode(),
        actualException.getError().getCode());
    assertFalse(actualException.getError().isRetriable());
  }

  @SuppressWarnings("java:S5778")
  @Test
  void parseTransactionThrowTest() {
    ApiException actualException = assertThrows(ApiException.class, () ->
        cardanoService.parseTransaction(NetworkEnum.PREVIEW.getNetwork(), TRANSACTION_SIGNED + "1", false));

    assertEquals(RosettaErrorType.INVALID_TRANSACTION.getMessage(),
        actualException.getError().getMessage());
    assertEquals(RosettaErrorType.INVALID_TRANSACTION.getCode(),
        actualException.getError().getCode());
    assertFalse(actualException.getError().isRetriable());
  }

  @SuppressWarnings("java:S5778")
  @Test
  void parseTransactionCborThrowTest() {
    try (MockedStatic<CborArrayToTransactionData> mocked = Mockito.mockStatic(
        CborArrayToTransactionData.class, Mockito.CALLS_REAL_METHODS)) {
      mocked.when(() -> CborArrayToTransactionData.convert(any(), Mockito.anyBoolean()))
          .thenThrow(new CborException("Test error"));

      ApiException actualException = assertThrows(ApiException.class, () ->
          cardanoService.parseTransaction(NetworkEnum.PREVIEW.getNetwork(), TRANSACTION_SIGNED, false));

      assertEquals(RosettaErrorType.INVALID_TRANSACTION.getMessage(),
          actualException.getError().getMessage());
      assertEquals(RosettaErrorType.INVALID_TRANSACTION.getCode(),
          actualException.getError().getCode());
      assertFalse(actualException.getError().isRetriable());
    }
  }

  @Test
  void submitTransactionTest() {
    //given
    ReflectionTestUtils.setField(cardanoService, "nodeSubmitApiPort", 8080);
    ReflectionTestUtils.setField(cardanoService, "cardanoNodeSubmitHost", "localhost");
    String txHash = IntStream.range(64, 128) //generate tx hash
        .mapToObj(Character::toChars)
        .map(String::new)
        .collect(Collectors.joining());
    HttpHeaders headers = new HttpHeaders();
    headers.add(Constants.CONTENT_TYPE_HEADER_KEY, Constants.CBOR_CONTENT_TYPE);
    ResponseEntity<String> resp = new ResponseEntity<>("\"" + txHash + "\"", headers, 202);
    when(restTemplate.postForEntity(anyString(),any(),eq(String.class))).thenReturn(resp);
    String hex = String.format("%040x", new BigInteger(1, "txToSign".getBytes()));
    //when
    String tx = cardanoService.submitTransaction(hex);
    //then
    assertEquals(txHash, tx);
    verify(restTemplate).postForEntity(
        "http://localhost:8080/api/submit/tx",
        new HttpEntity<>(HexUtil.decodeHexString(hex), headers),
        String.class);
  }

  @Test
  void submitTransactionTest_tx_error() {
    //given
    HttpHeaders headers = given();
    ResponseEntity<String> resp = new ResponseEntity<>("err", headers, 202);
    when(restTemplate.postForEntity(anyString(),any(),eq(String.class))).thenReturn(resp);
    String hex = String.format("%040x", new BigInteger(1, "txToSign".getBytes()));
    //when
    try {
      cardanoService.submitTransaction(hex);
    } catch (ApiException e) {
      //then
      Error error = e.getError();
      assertEquals(5006, error.getCode());
      assertEquals("Error when sending the transaction", error.getMessage());
      assertEquals("Transaction hash format error: err", error.getDescription());
    }
  }

  @Test
  void submitTransactionTest_tx_submit_error() {
    //given
    HttpHeaders headers = given();
    ResponseEntity<String> resp = new ResponseEntity<>("err", headers, 500);
    when(restTemplate.postForEntity(anyString(),any(),eq(String.class))).thenReturn(resp);
    String hex = String.format("%040x", new BigInteger(1, "txToSign".getBytes()));
    //when
    try {
      cardanoService.submitTransaction(hex);
    } catch (ApiException e) {
      //then
      Error error = e.getError();
      assertEquals(5006, error.getCode());
      assertEquals("Error when sending the transaction", error.getMessage());
      assertEquals("Transaction submit error: err", error.getDescription());
    }
  }

  @Test
  void getCardanoBaseAddressTest() {
    PublicKey stakingCredential = givenPublicKey();
    PublicKey publicKey = givenPublicKey();

    String cardanoAddress = cardanoService
        .getCardanoAddress(BASE, stakingCredential, publicKey, PREPROD);

    assertEquals("addr_test1qza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7amgrc6v3au3rqm66mn3kuwke340kfxga82tl7kh2nke8aslzyvu5",
        cardanoAddress);
  }

  @Test
  void getCardanoBaseAddressMissingPubKeyTest() {
    ApiException exception = assertThrows(ApiException.class,
        () -> cardanoService.getCardanoAddress(BASE, null, null, PREPROD));

    assertEquals("Public key is missing", exception.getError().getMessage());
  }

  @Test
  @SuppressWarnings("java:S5778")
  void getCardanoBaseAddressMissingStakingTest()  {
    ApiException exception = assertThrows(ApiException.class,
        () -> cardanoService.getCardanoAddress(BASE, null, new PublicKey(), PREPROD));

    assertEquals("Staking key is required for this type of address", exception.getError().getMessage());
  }

  @Test
  void getCardanoRewardAddressTest() {
    PublicKey stakingCredential = givenPublicKey();
    PublicKey publicKey = givenPublicKey();

    String cardanoAddress = cardanoService
        .getCardanoAddress(REWARD, stakingCredential, publicKey, PREPROD);

    assertEquals("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef", cardanoAddress);
  }

  @Test
  void getCardanoRewardAddressWOStakingTest() {
    PublicKey publicKey = givenPublicKey();

    String cardanoAddress = cardanoService
        .getCardanoAddress(REWARD, null, publicKey, PREPROD);

    assertEquals("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef", cardanoAddress);
  }

  @Test
  @SuppressWarnings("java:S5778")
  void getCardanoNullAddressTest() {
    ApiException exception = assertThrows(ApiException.class,
        () -> cardanoService.getCardanoAddress(AddressType.POOL_KEY_HASH,
            null, new PublicKey(), PREPROD));

    assertEquals("Provided address type is invalid", exception.getError().getMessage());
  }

  @Test
  void getHdPublicKeyFromRosettaKeyTest() {
    PublicKey publicKey = new PublicKey("48656C6C6F2C20776F726C6421", EDWARDS25519);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> cardanoService.getHdPublicKeyFromRosettaKey(publicKey));

    assertEquals("Invalid public key length", exception.getMessage());
  }

  @Test
  void buildTransaction() {
    List<Signatures> signatures = Collections.singletonList(givenSignatures());
    String result = cardanoService.buildTransaction(COMBINE_UNSIGNED_TRANSACTION, signatures);

    assertEquals(encodeHexString(blake2bHash256(decodeHexString(COMBINE_SIGNED_TRANSACTION))), encodeHexString(blake2bHash256(decodeHexString(result))));
  }

  @Test
  void buildTransaction_whenCannotDeserializeTransactionBody_thenThrowException() {
    List<Signatures> signatures = Collections.singletonList(givenSignatures());

    try (MockedStatic<CborSerializationUtil> mocked = Mockito.mockStatic(CborSerializationUtil.class)) {
      mocked.when(() -> CborSerializationUtil.deserialize(any(byte[].class)))
          .thenThrow(new CborRuntimeException("CborException"));

      ApiException result = assertThrows(ApiException.class,
          () -> cardanoService.buildTransaction(COMBINE_UNSIGNED_TRANSACTION, signatures));

      assertEquals(RosettaErrorType.CANT_CREATE_SIGN_TRANSACTION.getMessage(), result.getError().getMessage());
      assertEquals(RosettaErrorType.CANT_CREATE_SIGN_TRANSACTION.getCode(), result.getError().getCode());
      assertFalse(result.getError().isRetriable());
      mocked.verify(() -> CborSerializationUtil.deserialize(any(byte[].class)), times(1));
    }
  }

  @Test
  void calculateRosettaSpecificTransactionFeeTest() {
    List<BigInteger> inputAmounts = List.of(BigInteger.valueOf(5L));
    List<BigInteger> outputAmounts = List.of(BigInteger.valueOf(2L));
    List<BigInteger> withdrawalAmounts = List.of(BigInteger.valueOf(5L));

    Map<String, Double> depositsSumMap = Map.of(
            Constants.KEY_REFUNDS_SUM, (double) 6L,
            Constants.KEY_DEPOSITS_SUM, (double) 2L,
            Constants.POOL_DEPOSITS_SUM, (double) 2L
    );

    var calculatedFee = cardanoService.calculateRosettaSpecificTransactionFee(
            inputAmounts,
            outputAmounts,
            withdrawalAmounts,
            depositsSumMap
    );

    assertEquals(0, calculatedFee);
  }

  @Test
  void negativeWithdrawalsShouldNotBeAccepted() {
    List<BigInteger> inputAmounts = List.of(BigInteger.valueOf(-5L));
    List<BigInteger> outputAmounts = List.of(BigInteger.valueOf(2L));
    List<BigInteger> withdrawalAmounts = List.of(BigInteger.valueOf(-7L));

    Map<String, Double> depositsSumMap = Map.of(Constants.KEY_REFUNDS_SUM, (double) 6L,
            Constants.KEY_DEPOSITS_SUM, (double) 2L, Constants.POOL_DEPOSITS_SUM, (double) 2L);

    ApiException exception = assertThrows(ApiException.class,
            () -> cardanoService.calculateRosettaSpecificTransactionFee(inputAmounts, outputAmounts, withdrawalAmounts, depositsSumMap));

    assertEquals(5044, exception.getError().getCode());
    assertEquals("Withdrawal amounts cannot be negative", exception.getError().getMessage());
  }

  @Test
  void valueOfOutputsIsBiggerThanValueOfInputs() {
    List<BigInteger> inputAmounts = List.of(BigInteger.valueOf(-5L));
    List<BigInteger> outputAmounts = List.of(BigInteger.valueOf(1000L));
    List<BigInteger> withdrawalAmounts = List.of(BigInteger.valueOf(1L));

    Map<String, Double> depositsSumMap = Map.of(
            Constants.KEY_REFUNDS_SUM, (double) 6L,
            Constants.KEY_DEPOSITS_SUM, (double) 2L,
            Constants.POOL_DEPOSITS_SUM, (double) 2L);

    ApiException exception = assertThrows(ApiException.class,
            () -> cardanoService.calculateRosettaSpecificTransactionFee(inputAmounts, outputAmounts, withdrawalAmounts, depositsSumMap));

    assertEquals(4010, exception.getError().getCode());
    assertEquals("The transaction you are trying to build has more value in outputs than value of inputs", exception.getError().getMessage());
  }

  @NotNull
  private HttpHeaders given() {
    ReflectionTestUtils.setField(cardanoService, "nodeSubmitApiPort", 8080);
    ReflectionTestUtils.setField(cardanoService, "cardanoNodeSubmitHost", "localhost");

    HttpHeaders headers = new HttpHeaders();
    headers.add(Constants.CONTENT_TYPE_HEADER_KEY, Constants.CBOR_CONTENT_TYPE);

    return headers;
  }

}
