package org.cardanofoundation.rosetta.api.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;

import java.net.UnknownHostException;
import java.util.List;

public interface MempoolMonitoringService {
  MempoolResponse getAllTransaction(NetworkRequest networkRequest);

  List<byte[]> getAllTransactionsInMempool();
  MempoolTransactionResponse getDetailTransaction(MempoolTransactionRequest mempoolTransactionRequest)
          throws CborException, CborDeserializationException, UnknownHostException
          , AddressExcepion, CborSerializationException, JsonProcessingException;
}
