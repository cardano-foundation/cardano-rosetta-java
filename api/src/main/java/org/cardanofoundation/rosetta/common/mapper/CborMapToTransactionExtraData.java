package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import java.util.ArrayList;
import java.util.List;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.Operation;

public class CborMapToTransactionExtraData {

  public static TransactionExtraData convertCborMapToTransactionExtraData(Map map) {
    String transactionMetadataHex = getTransactionMetadataHexFromMap(map);

    List<Operation> operations = new ArrayList<>();
    List<DataItem> operationsListMap = ((Array) map.get(
        new UnicodeString(Constants.OPERATIONS))).getDataItems();
    operationsListMap.forEach(oDataItem -> {
      Map operationMap = (Map) oDataItem;
      Operation operation = CborMapToOperation.cborMapToOperation(operationMap);
      operations.add(operation);
    });
    return new TransactionExtraData(operations, transactionMetadataHex);
  }

  private static String getTransactionMetadataHexFromMap(Map map) {
    DataItem transactionMetadataHex = map.get(new UnicodeString(Constants.TRANSACTIONMETADATAHEX));
    return transactionMetadataHex == null ? ""
        : ((UnicodeString) transactionMetadataHex).getString();
  }
}
