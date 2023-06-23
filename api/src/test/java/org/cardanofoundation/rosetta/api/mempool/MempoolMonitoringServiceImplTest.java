package org.cardanofoundation.rosetta.api.mempool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.cardanofoundation.rosetta.api.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.model.Amount;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.OperationIdentifier;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.impl.MempoolMonitoringServiceImpl;
import org.cardanofoundation.rosetta.api.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.api.util.ConVertConstructionUtil;
import org.cardanofoundation.rosetta.api.util.RosettaConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class MempoolMonitoringServiceImplTest {

  @Mock
  private LocalTxMonitorClient localTxMonitorClient;
  @Mock
  private CardanoService cardanoService;

  @Mock
  private ConVertConstructionUtil conVertConstructionUtil;
  @InjectMocks
  private MempoolMonitoringServiceImpl mempoolMonitoringService;
  private NetworkRequest networkRequest;
  private MempoolTransactionRequest mempoolTransactionRequest;
  private List<byte[]> txBytesList;

  @BeforeEach
  void setUp() {
    NetworkIdentifier identifier = NetworkIdentifier.builder()
        .blockchain(RosettaConstants.BLOCKCHAIN_NAME)
        .network(RosettaConstants.MAINNET).build();
    networkRequest = new NetworkRequest();
    networkRequest.setNetworkIdentifier(identifier);
    txBytesList = new ArrayList<>();
    txBytesList.add(HexUtil.decodeHexString(
        "84a400818258202f23fd8cca835af21f3ac375bac601f97ead75f2e79143bdf71fe2c4be043e8f01018282581d61bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb19271082581d61bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb199c4002199c40031903e8a100818258201b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f58406c92508135cb060187a2706ade8154782867b1526e9615d06742be5c56f037ab85894c098c2ab07971133c0477baee92adf3527ad7cc816f13e1e4c361041206f5f6"));

    txBytesList.add(HexUtil.decodeHexString(
        "84a40081825820f46e97cf570727fbd9398046f913d76379cb021d6ff658dfcc90b582100bbaae000181825839000743d16cfe3c4fcc0c11c2403bbc10dbc7ecdd4477e053481a368e7a06e2ae44dff6770dc0f4ada3cf4cf2605008e27aecdb332ad349fda71a009f7450021a00086470031a01db051aa10081825820afe915d5d360f50446e4346355f63e96537448fa6e01511a11687f30fad00c40584053194e3ed7360af1f8da3ddd31ce9a9a37da1c35a0ef7e364b3843a62397215ea7cd75ba1b6e64dd2f56babb11458bea1a4fe99c82da92cc17ca60059bdd3907f5f6"));
    mempoolTransactionRequest = new MempoolTransactionRequest();
    mempoolTransactionRequest.setNetworkIdentifier(identifier);
  }

  @Test
  void test_MempoolMonitoringService_getAllTransaction_returns_empty_when_dont_have_transactions_in_mempool() {
    Mockito.when(localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono())
        .thenReturn(Mono.empty());
    MempoolResponse mempoolResponse = mempoolMonitoringService.getAllTransaction(networkRequest);

    assertEquals(0, mempoolResponse.getTransactionIdentifierList().size());
  }

  @Test
  void test_MempoolMonitoringService_getAllTransaction_returns_transactions_hash_when_have_transactions_in_mempool() {
    Mockito.when(localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono())
        .thenReturn(Mono.just(txBytesList));
    MempoolResponse mempoolResponse = mempoolMonitoringService.getAllTransaction(networkRequest);

    assertEquals(2, mempoolResponse.getTransactionIdentifierList().size());
    assertEquals("333a6ccaaa639f7b451ce93764f54f654ef499fdb7b8b24374ee9d99eab9d795",
        mempoolResponse.getTransactionIdentifierList().get(0).getHash());
    assertEquals("e8c9690479cde4db28030fc1b65ac258d274f37393309e1dd587d5334fcf4787",
        mempoolResponse.getTransactionIdentifierList().get(1).getHash());

  }
  @Test
  void test_MempoolMornitoringService_GetDetailTransaction_return_detail_transaction_when_have_transaction_in_mempool()
          throws UnknownHostException, CborException, AddressExcepion, CborDeserializationException,
          CborSerializationException, JsonProcessingException {
    String txHash = TransactionUtil.getTxHash(txBytesList.get(1));
    Mockito.when(localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono())
            .thenReturn(Mono.just(txBytesList));
    Mockito.when(cardanoService.getNetworkIdentifierByRequestParameters(mempoolTransactionRequest.getNetworkIdentifier()))
            .thenReturn(NetworkIdentifierType.CARDANO_MAINNET_NETWORK);
    TransactionIdentifier transactionIdentifier = new TransactionIdentifier(txHash);
    mempoolTransactionRequest.setTransactionIdentifier(transactionIdentifier);
    List<Operation> operations = new ArrayList<>();

    Operation o1 = new Operation();
    o1.setOperationIdentifier(new OperationIdentifier(1L , null));
    o1.setRelatedOperations(List.of(new OperationIdentifier(0L,null)));
    o1.setType("output");
    o1.setStatus("");
    o1.setAccount(new AccountIdentifier("addr_test1qqr585tvlc7ylnqvz8pyqwauzrdu0mxag3m7q56grgmgu7sxu2hyfhlkwuxupa9d5085eunq2qywy7hvmvej456flknswgndm3",
        null,
        null));
    o1.setAmount(new Amount("10450000",new Currency("ADA" , 6 , null) , null));
    o1.setCoinChange(null);
    o1.setMetadata(null);

    Operation o2 = new Operation();
    o2.setOperationIdentifier(null);
    o2.setRelatedOperations(null);
    o2.setType(null);
    o2.setStatus(null);
    o2.setAccount(null);
    o2.setAmount(null);
    o2.setCoinChange(null);
    o2.setMetadata(null);


    operations.add(o1);
    operations.add(o2);

    doReturn(operations).when(conVertConstructionUtil).convert(any(TransactionBody.class) ,
        any(TransactionExtraData.class),
        any());
    try (MockedStatic<ConVertConstructionUtil> theMock = Mockito.mockStatic(ConVertConstructionUtil.class)) {
      theMock.when(() -> ConVertConstructionUtil.convert(any(TransactionBody.class) ,
              any(TransactionExtraData.class),
              any()))
          .thenReturn(operations);
    }
    MempoolTransactionResponse expected = MempoolTransactionResponse.builder().
            transaction(
                    new org.cardanofoundation.rosetta.api.model.Transaction(
                            new TransactionIdentifier(txHash),
                            operations)
            )
            .build();

    MempoolTransactionResponse actual = mempoolMonitoringService.getDetailTransaction(mempoolTransactionRequest);
    assertEquals(expected.getTransaction().getTransactionIdentifier(), actual.getTransaction().getTransactionIdentifier());
    assertEquals(2 , actual.getTransaction().getOperations().size());


  }
}
