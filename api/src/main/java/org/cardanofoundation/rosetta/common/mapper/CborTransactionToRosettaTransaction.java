package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import java.util.List;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.RawTransaction;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.openapitools.client.model.Operation;

public class CborTransactionToRosettaTransaction {

  public static org.openapitools.client.model.Transaction convert(RawTransaction rawTx) {
    Transaction deserialize;
    try {
      deserialize = Transaction.deserialize(rawTx.txbytes());
    } catch (CborDeserializationException e) {
      throw new RuntimeException(e);
    }

    DataItem dataItem = CborSerializationUtil.deserialize(rawTx.txbytes());
    TransactionData convert;
    List<Operation> operationList;
    try {
      convert = CborArrayToTransactionData.convert((Array) dataItem, false);
      operationList = TransactionDataToOperations.convert(convert, NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue());
    } catch (CborException | CborDeserializationException | CborSerializationException e) {
      throw new RuntimeException(e);
    }

    return org.openapitools.client.model.Transaction.builder()
        .transactionIdentifier(org.openapitools.client.model.TransactionIdentifier
            .builder()
            .hash(rawTx.txhash())
            .build())
        .operations(operationList)
        .build();
  }

}
