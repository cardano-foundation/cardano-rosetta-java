package org.cardanofoundation.rosetta.api.construction.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.openapitools.client.model.Operation;

import java.util.List;

/**
 * Parser interface for extracting Rosetta operations from Cardano transactions.
 * <p>
 * This parser handles the conversion between Cardano transaction structures and Rosetta API
 * operations, including inputs, outputs, certificates (staking, pool, governance), and withdrawals.
 * </p>
 */
public interface TransactionOperationParser {

  /**
   * Extracts and constructs a complete list of Rosetta operations from transaction data.
   * <p>
   * This method processes all operation types from a Cardano transaction, including:
   * <ul>
   *   <li>Input operations - UTxO consumption</li>
   *   <li>Output operations - UTxO creation</li>
   *   <li>Certificate operations - Staking registrations, delegations, pool operations</li>
   *   <li>Governance operations - DRep vote delegations, pool governance votes</li>
   *   <li>Withdrawal operations - Staking reward withdrawals</li>
   * </ul>
   * </p>
   *
   * @param data the transaction data containing the transaction body and extra metadata
   * @param network the Cardano network (mainnet/testnet) for address generation
   * @return a list of Rosetta operations representing all transaction activities
   * @throws CborDeserializationException if CBOR deserialization fails
   * @throws CborException if CBOR processing encounters an error
   * @throws CborSerializationException if CBOR serialization fails
   */
  List<Operation> getOperationsFromTransactionData(TransactionData data, Network network)
      throws CborDeserializationException, CborException, CborSerializationException;

  /**
   * Determines the required signers for a given operation.
   * <p>
   * Different operation types require different signers:
   * <ul>
   *   <li>Pool operations - Pool owners, reward address, and optionally payment address</li>
   *   <li>Staking operations - Stake address derived from staking credential</li>
   *   <li>Regular operations - Account address from the operation</li>
   * </ul>
   * </p>
   *
   * @param network the Cardano network for address generation
   * @param operation the operation to extract signers from
   * @return list of addresses that must sign this operation
   */
  List<String> getSignerFromOperation(Network network, Operation operation);

}
