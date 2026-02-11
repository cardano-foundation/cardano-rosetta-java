package org.cardanofoundation.rosetta.common.exception;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

public class ExceptionFactory {

    public static ApiException blockNotFoundException() {
        return new ApiException(RosettaErrorType.BLOCK_NOT_FOUND.toRosettaError());
    }

    public static ApiException genesisBlockNotFound() {
        return new ApiException(RosettaErrorType.GENESIS_BLOCK_NOT_FOUND.toRosettaError());
    }

    public static ApiException configNotFoundException(String path) {
        return new ApiException(RosettaErrorType.CONFIG_NOT_FOUND.toRosettaError(
                Details.builder().message(path).build()));
    }

    public static ApiException networkNotFoundError() {
        return new ApiException(RosettaErrorType.NETWORK_NOT_FOUND.toRosettaError());
    }

    public static ApiException unspecifiedError(String details) {
        return new ApiException(RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(
                Details.builder().message(details).build()));
    }

    public static ApiException invalidAddressError(String address) {
        return new ApiException(RosettaErrorType.INVALID_ADDRESS.toRosettaError(
                Details.builder().message(address).build()));
    }

    public static ApiException invalidAddressCasingError(String address) {
        return new ApiException(RosettaErrorType.INVALID_ADDRESS_CASING.toRosettaError(
                Details.builder().message(address).build()));
    }

    public static ApiException missingStakingKeyError() {
        return new ApiException(RosettaErrorType.STAKING_KEY_MISSING.toRosettaError());
    }

    public static ApiException invalidBlockchainError() {
        return new ApiException(RosettaErrorType.INVALID_BLOCKCHAIN.toRosettaError());
    }

    public static ApiException invalidAddressError() {
        return new ApiException(RosettaErrorType.INVALID_ADDRESS.toRosettaError());
    }

    public static ApiException cantCreateSignTransaction() {
        return new ApiException(RosettaErrorType.CANT_CREATE_SIGN_TRANSACTION.toRosettaError());
    }

    public static ApiException missingChainCodeError() {
        return new ApiException(RosettaErrorType.CHAIN_CODE_MISSING.toRosettaError());
    }

    public static ApiException totalOutputValuesIsBiggerThanTotalInputValues() {
        return new ApiException(
                RosettaErrorType.OUTPUTS_BIGGER_THAN_INPUTS_ERROR.toRosettaError());
    }

    public static ApiException invalidOperationTypeError() {
        return new ApiException(RosettaErrorType.INVALID_OPERATION_TYPE.toRosettaError());
    }

    public static ApiException missingPoolKeyError() {
        return new ApiException(RosettaErrorType.POOL_KEY_MISSING.toRosettaError());
    }

    public static ApiException missingPoolCertError() {
        return new ApiException(RosettaErrorType.POOL_CERT_MISSING.toRosettaError());
    }

    public static ApiException invalidPoolRegistrationCert(String error) {
        return new ApiException(RosettaErrorType.INVALID_POOL_CERT.toRosettaError(
                Details.builder().message(error).build()));
    }

    public static ApiException invalidPoolRegistrationCertType() {
        return new ApiException(RosettaErrorType.INVALID_POOL_CERT_TYPE.toRosettaError());
    }

    public static ApiException publicKeyMissing() {
        return new ApiException(RosettaErrorType.PUBLIC_KEY_MISSING.toRosettaError());
    }

    public static ApiException invalidStakingKeyFormat() {
        return new ApiException(RosettaErrorType.INVALID_STAKING_KEY_FORMAT.toRosettaError());
    }

    public static ApiException missingMetadataParametersForPoolRetirement() {
        return new ApiException(
                RosettaErrorType.MISSING_METADATA_PARAMETERS_FOR_POOL_RETIREMENT.toRosettaError());
    }

    public static ApiException invalidPoolKeyError(String error) {
        return new ApiException(RosettaErrorType.INVALID_POOL_KEY_HASH.toRosettaError(
                Details.builder().message(error).build()));
    }

    public static ApiException missingPoolRegistrationParameters() {
        return new ApiException(
                RosettaErrorType.POOL_REGISTRATION_PARAMS_MISSING.toRosettaError());
    }

    public static ApiException invalidPoolMetadataError(String error) {
        return new ApiException(RosettaErrorType.INVALID_POOL_METADATA.toRosettaError(
                Details.builder().message(error).build()));
    }

    public static ApiException invalidPoolRelaysError() {
        return new ApiException(RosettaErrorType.INVALID_POOL_RELAYS.toRosettaError());
    }

    public static ApiException invalidPoolRelayTypeError() {
        return new ApiException(RosettaErrorType.INVALID_POOL_RELAY_TYPE.toRosettaError());
    }

    public static ApiException invalidIpv4() {
        return unspecifiedError("Ipv4 has an invalid format");
    }

    public static ApiException missingDnsNameError() {
        return new ApiException(RosettaErrorType.DNS_NAME_MISSING.toRosettaError());
    }

    public static ApiException invalidPoolRelaysError(String value) {
        String error = "Given value " + value + " is invalid";
        return new ApiException(RosettaErrorType.INVALID_POOL_RELAYS.toRosettaError(
                Details.builder().message(error).build()));
    }

    public static ApiException invalidPoolRegistrationParameters(String value) {
        return new ApiException(RosettaErrorType.INVALID_POOL_REGISTRATION_PARAMS.toRosettaError(
                Details.builder().message(value).build()));
    }

    public static ApiException transactionInputsParametersMissingError(String error) {
        return new ApiException(
                RosettaErrorType.TRANSACTION_INPUTS_PARAMETERS_MISSING_ERROR.toRosettaError(
                        Details.builder().message(error).build()));
    }

    public static ApiException transactionOutputDeserializationError(String details) {
        return new ApiException(
                RosettaErrorType.TRANSACTION_OUTPUT_DESERIALIZATION_ERROR.toRosettaError(
                        Details.builder().message(details).build()));
    }

    public static ApiException transactionOutputsParametersMissingError(String error) {
        return new ApiException(
                RosettaErrorType.TRANSACTION_OUTPUTS_PARAMETERS_MISSING_ERROR.toRosettaError(
                        Details.builder().message(error).build()));
    }

    public static ApiException cantCreateUnsignedTransactionFromBytes() {
        return new ApiException(
                RosettaErrorType.CANT_CREATE_UNSIGNED_TRANSACTION_ERROR.toRosettaError());
    }

    public static ApiException cantEncodeExtraData() {
        return new ApiException(RosettaErrorType.CANT_ENCODE_EXTRA_DATA.toRosettaError());
    }

    public static ApiException cantCreateSignedTransactionFromBytes() {
        return new ApiException(
                RosettaErrorType.CANT_CREATE_SIGNED_TRANSACTION_ERROR.toRosettaError());
    }

    public static ApiException tokenBundleAssetsMissingError() {
        return new ApiException(RosettaErrorType.TOKEN_BUNDLE_ASSETS_MISSING.toRosettaError());
    }

    public static ApiException tokenAssetValueMissingError() {
        return new ApiException(RosettaErrorType.TOKEN_ASSET_VALUE_MISSING.toRosettaError());
    }

    public static ApiException parseSignedTransactionError() {
        return new ApiException(RosettaErrorType.PARSE_SIGNED_TRANSACTION_ERROR.toRosettaError());
    }

    public static ApiException invalidPoolOwnersError(String error) {
        return new ApiException(RosettaErrorType.INVALID_POOL_OWNERS.toRosettaError(
                Details.builder().message(error).build()));
    }

    public static ApiException invalidAddressTypeError() {
        return new ApiException(RosettaErrorType.INVALID_ADDRESS_TYPE.toRosettaError());
    }

    public static ApiException transactionNotFound() {
        return new ApiException(RosettaErrorType.TRANSACTION_NOT_FOUND.toRosettaError());
    }

    public static ApiException invalidTokenNameError(String details) {
        return new ApiException(RosettaErrorType.INVALID_TOKEN_NAME.toRosettaError(
                Details.builder().message(details).build()));
    }

    public static ApiException invalidPolicyIdError(String details) {
        return new ApiException(RosettaErrorType.INVALID_POLICY_ID.toRosettaError(
                Details.builder().message(details).build()));
    }

    public static ApiException deserializationError(String details) {
        return new ApiException(
                RosettaErrorType.TRANSACTION_INPUT_DESERIALIZATION_ERROR.toRosettaError(
                        Details.builder().message(details).build()));
    }

    public static ApiException invalidNetworkError() {
        return new ApiException(RosettaErrorType.INVALID_NETWORK.toRosettaError());
    }

    public static ApiException sendTransactionError(String error) {
        return new ApiException(
                RosettaErrorType.SEND_TRANSACTION_ERROR.toRosettaError(null, error));
    }

    public static ApiException invalidTransactionError() {
        return new ApiException(RosettaErrorType.INVALID_TRANSACTION.toRosettaError());
    }

    public static ApiException generalSerializationError(String details) {
        return new ApiException(
                RosettaErrorType.SERIALIZATION_ERROR.toRosettaError(new Details(details)));
    }

    public static ApiException generalDeserializationError(String details) {
        return new ApiException(
                RosettaErrorType.DESERIALIZATION_ERROR.toRosettaError(new Details(details)));
    }

    public static ApiException ttlMissingError() {
        return new ApiException(RosettaErrorType.TTL_MISSING.toRosettaError());
    }

    private ExceptionFactory() {
    }

    public static ApiException protocolParametersMissingError() {
        return new ApiException(RosettaErrorType.PROTOCOL_PARAMETERS_MISSING.toRosettaError());
    }

    public static ApiException coinsPerUtxoSizeMissingError() {
        return new ApiException(RosettaErrorType.COINS_PER_UTXO_SIZE_MISSING.toRosettaError());
    }

    public static ApiException maxTxSizeMissingError() {
        return new ApiException(RosettaErrorType.MAX_TX_SIZE_MISSING.toRosettaError());
    }

    public static ApiException maxValSizeMissingError() {
        return new ApiException(RosettaErrorType.MAX_VAL_SIZE_MISSING.toRosettaError());
    }

    public static ApiException keyDepositMissingError() {
        return new ApiException(RosettaErrorType.KEY_DEPOSIT_MISSING.toRosettaError());
    }

    public static ApiException maxCollateralInputsMissingError() {
        return new ApiException(RosettaErrorType.MAX_COLLATERAL_INPUTS_MISSING.toRosettaError());
    }

    public static ApiException minFeeCoefficientMissingError() {
        return new ApiException(RosettaErrorType.MIN_FEE_COEFFICIENT_MISSING.toRosettaError());
    }

    public static ApiException minFeeConstantMissingError() {
        return new ApiException(RosettaErrorType.MIN_FEE_CONSTANT_MISSING.toRosettaError());
    }

    public static ApiException minPoolCostMissingError() {
        return new ApiException(RosettaErrorType.MIN_POOL_COST_MISSING.toRosettaError());
    }

    public static ApiException protocolMissingError() {
        return new ApiException(RosettaErrorType.PROTOCOL_MISSING.toRosettaError());
    }

    public static ApiException poolDepositMissingError() {
        return new ApiException(RosettaErrorType.POOL_DEPOSIT_MISSING.toRosettaError());
    }

    public static ApiException notSupportedInOfflineMode() {
        return new ApiException(RosettaErrorType.NOT_SUPPORTED_IN_OFFLINE_MODE.toRosettaError());
    }

    public static ApiException gatewayError() {
        return new ApiException(RosettaErrorType.GATEWAY_ERROR.toRosettaError());
    }

    public static ApiException misconfiguredTime(LocalDateTime now) {
        return new ApiException(RosettaErrorType.MISCONFIGURED_TIME.toRosettaError(Details.builder().message("Current time: %s".formatted(now)).build()));
    }

    public static ApiException missingDRepId() {
        return new ApiException(RosettaErrorType.MISSING_DREP_ID.toRosettaError());
    }

    public static ApiException timeOut(String detailMessage) {
        return new ApiException(RosettaErrorType.TIMEOUT.toRosettaError(Details.builder().message("Timeout, details: %s".formatted(detailMessage)).build()));
    }

    public static ApiException invalidBlockIdentifier(@NotNull long index) {
        return new ApiException(RosettaErrorType.INVALID_BLOCK_INDEX.toRosettaError(Details.builder().message("Invalid block index, must be greater than or equal to 0, supplied index: %d".formatted(index)).build()));
    }

    public static ApiException oldestBlockNotFound() {
        return new ApiException(RosettaErrorType.OLDEST_BLOCK_NOT_FOUND.toRosettaError());
    }

    public static ApiException withdrawalAmountsNegativeError() {
        return new ApiException(RosettaErrorType.WITHDRAWAL_AMOUNT_CANNOT_BE_NEGATIVE.toRosettaError());
    }

    public static ApiException invalidDrepIdLength() {
        return new ApiException(RosettaErrorType.INVALID_DREP_ID_LENGTH.toRosettaError());
    }

    public static ApiException mismatchDrepType() {
        return new ApiException(RosettaErrorType.MISMATCH_DREP_TYPE.toRosettaError());
    }

    public static ApiException missingDrep() {
        return new ApiException(RosettaErrorType.MISSING_DREP_TYPE.toRosettaError());
    }

    public static ApiException invalidDrepType() {
        return new ApiException(RosettaErrorType.INVALID_DREP_TYPE.toRosettaError());
    }

    public static ApiException invalidGovernanceVote(String reasonText) {
        Error error = Error.builder()
                .code(RosettaErrorType.GOVERNANCE_INVALID_VOTE.getCode())
                .message("Invalid governance vote, reason: %s".formatted(reasonText))
                .retriable(RosettaErrorType.GOVERNANCE_INVALID_VOTE.isRetriable())
                .build();
        return new ApiException(error);
    }

    public static ApiException callMethodNotSupported() {
        return new ApiException(RosettaErrorType.CALL_METHOD_NOT_SUPPORTED.toRosettaError());
    }

    public static ApiException callParameterMissing(String paramStatus) {
        return new ApiException(RosettaErrorType.CALL_PARAMETER_MISSING.toRosettaError(
                Details.builder().message("Parameter '%s' is missing".formatted(paramStatus)).build()));
    }

    public static ApiException invalidBlockErrorReviewStatus() {
        return new ApiException(RosettaErrorType.INVALID_BLOCK_ERROR_REVIEW_STATUS.toRosettaError(
                Details.builder().message("Parameter 'status' must be one of: [REVIEWED_AFFECTS_US, REVIEWED_DOES_NOT_AFFECT_US]").build()));
    }

    public static ApiException invalidLimitSize(Long limit, Long maxLimitSize) {
        return new ApiException(RosettaErrorType.INVALID_LIMIT.toRosettaError(
                Details.builder().message("Requested limit size %d exceeds maximum allowed size %d".formatted(limit, maxLimitSize)).build()));
    }

    public static ApiException bothSuccessAndStatusProvided() {
        return new ApiException(RosettaErrorType.BOTH_SUCCESS_AND_STATUS_PROVIDED.toRosettaError());
    }

    public static ApiException bothAccountAndAccountIdentifierProvided() {
        return new ApiException(RosettaErrorType.BOTH_ACCOUNT_AND_ACCOUNT_IDENTIFIER_PROVIDED.toRosettaError(
                Details.builder().message("Both 'account' and 'account_identifier' parameters are provided, only one is allowed.").build()));
    }

    public static ApiException operationTypeSearchNotSupported(String type) {
        return new ApiException(RosettaErrorType.OPERATION_TYPE_SEARCH_NOT_SUPPORTED.toRosettaError(
                Details.builder().message("Operation type: '" + type + "'").build()));
    }

    public static ApiException invalidOperationStatus(String status) {
        return new ApiException(RosettaErrorType.INVALID_OPERATION_STATUS.toRosettaError(
                Details.builder().message("Invalid operation status: '" + status + "'. Supported values are: 'success', 'invalid', 'true', 'false'").build()));
    }

    public static ApiException currencySymbolNotHex(String symbol) {
        return new ApiException(RosettaErrorType.CURRENCY_SYMBOL_NOT_HEX.toRosettaError(
                Details.builder().message("Currency symbol must be hex-encoded, but got: '" + symbol + "'").build()));
    }

}
