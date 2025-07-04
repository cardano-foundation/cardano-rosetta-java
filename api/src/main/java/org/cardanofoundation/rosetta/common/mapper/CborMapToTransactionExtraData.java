package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CborMapToTransactionExtraData {

  private static final UnicodeString OPERATIONS = new UnicodeString(Constants.OPERATIONS);

  private CborMapToTransactionExtraData() {
  }

  public static TransactionExtraData convertCborMapToTransactionExtraData(Map map) {
    List<Operation> operations = new ArrayList<>();
    Array dataItems = (Array) map.get(OPERATIONS);
    if (Objects.isNull(dataItems)) {
      throw ExceptionFactory.parseSignedTransactionError();
    }

    List<DataItem> operationsListMap = dataItems.getDataItems();
    operationsListMap.forEach(oDataItem -> {
      Map operationMap = (Map) oDataItem;
      Operation operation = CborMapToOperation.cborMapToOperation(operationMap);
      operations.add(operation);
    });

    return new TransactionExtraData(operations);
  }

}
