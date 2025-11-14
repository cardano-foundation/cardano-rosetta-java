package org.cardanofoundation.rosetta.common.util;

import com.google.common.collect.ImmutableList;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;

import java.util.List;

import static org.cardanofoundation.rosetta.common.enumeration.OperationType.STAKE_DELEGATION;
import static org.cardanofoundation.rosetta.common.util.Constants.OPERATION_TYPE_DREP_VOTE_DELEGATION;

public final class OperationTypes {

    public static final List<String> STAKING_OPERATIONS =
            List.of(STAKE_DELEGATION.getValue(),
                    OperationType.STAKE_KEY_REGISTRATION.getValue(),
                    OperationType.STAKE_KEY_DEREGISTRATION.getValue()
            );

    public static final List<String> POOL_OPERATIONS =
            List.of(OperationType.POOL_RETIREMENT.getValue(),
                    OperationType.POOL_REGISTRATION.getValue(),
                    OperationType.POOL_REGISTRATION_WITH_CERT.getValue());

    /**
     * All certificate-based operations that appear in the transaction body's certs[] array.
     * <p>
     * Includes staking operations (registration, deregistration, delegation),
     * pool operations (registration, retirement), and DRep vote delegation.
     * </p>
     * <p>
     * <b>Note:</b> Withdrawals are NOT certificates. SPO governance votes are voting procedures, not certificates.
     * </p>
     */
    public static final List<String> CERTIFICATE_OPERATIONS =
            ImmutableList.<String>builder()
                    .addAll(STAKING_OPERATIONS)
                    .addAll(POOL_OPERATIONS)
                    .add(OPERATION_TYPE_DREP_VOTE_DELEGATION)
                    .build();

}
