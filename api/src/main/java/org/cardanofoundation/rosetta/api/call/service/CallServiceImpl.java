package org.cardanofoundation.rosetta.api.call.service;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressType;
import com.bloxbean.cardano.client.address.util.AddressEncoderDecoderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorReviewEntity;
import org.cardanofoundation.rosetta.api.error.service.BlockParsingErrorReviewService;
import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.services.Cip113AddressService;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.openapitools.client.model.CallRequest;
import org.openapitools.client.model.CallResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;
import java.util.*;

import static org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus.UNREVIEWED;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallServiceImpl implements CallService {

    private static final String METHOD_GET_PARSE_ERROR_BLOCKS = "get_parse_error_blocks";
    private static final String METHOD_MARK_PARSE_ERROR_BLOCK_CHECKED = "mark_parse_error_block_checked";
    private static final String METHOD_RESOLVE_SMART_WALLET_ADDR = "resolve_smart_wallet_addr";
    private static final int CREDENTIAL_HASH_LENGTH = 28;
    private static final int ENTERPRISE_ADDRESS_LENGTH = 1 + CREDENTIAL_HASH_LENGTH;
    private static final int BASE_ADDRESS_LENGTH = 1 + (2 * CREDENTIAL_HASH_LENGTH);
    
    private final BlockParsingErrorReviewService blockParsingErrorReviewService;
    private final Cip113AddressService cip113AddressService;

    @Override
    public List<String> getSupportedMethods() {
        return List.of(METHOD_GET_PARSE_ERROR_BLOCKS, METHOD_MARK_PARSE_ERROR_BLOCK_CHECKED,
                METHOD_RESOLVE_SMART_WALLET_ADDR);
    }

    @Override
    public CallResponse processCallRequest(CallRequest callRequest) {
        String method = callRequest.getMethod();

        log.info("Processing call request for method: {}", method);

        return switch (method) {
            case METHOD_GET_PARSE_ERROR_BLOCKS -> getParseErrorBlocks(extractStatusParameter(callRequest.getParameters()).orElse(null));
            case METHOD_MARK_PARSE_ERROR_BLOCK_CHECKED -> markParseErrorBlockChecked(callRequest.getParameters());
            case METHOD_RESOLVE_SMART_WALLET_ADDR -> resolveSmartWalletAddress(callRequest);

            default -> throw ExceptionFactory.callMethodNotSupported();
        };
    }

    @Override
    public @NotNull CallResponse resolveSmartWalletAddress(@NotNull CallRequest callRequest) {
        String inputAddress = extractAddressParameter(callRequest.getParameters());
        NetworkEnum network = NetworkEnum.findByName(callRequest.getNetworkIdentifier().getNetwork())
                .orElseThrow(ExceptionFactory::invalidNetworkError);
        byte[] configuredScriptHash = cip113AddressService.getConfiguredScriptHash();

        EraAddressType eraAddressType = CardanoAddressUtils.getEraAddressType(inputAddress);
        if (eraAddressType == null) {
            throw ExceptionFactory.cip113InvalidAddress(
                    "The provided address is malformed or has an unknown era");
        }
        if (eraAddressType == EraAddressType.BYRON) {
            throw ExceptionFactory.cip113AddressTypeNotSupported(
                    "Byron addresses are not supported");
        }

        Address address = new Address(inputAddress);
        // A bech32 payload whose header nibble marks it as Byron parses, but cardano-client-lib
        // has no HRP for that type, so it must be rejected before the canonical-prefix check.
        if (address.getAddressType() == AddressType.Byron) {
            throw ExceptionFactory.cip113AddressTypeNotSupported(
                    "Byron addresses are not supported");
        }

        requireCanonicalPrefix(address);

        if (address.getNetwork().getNetworkId() != network.getNetwork().getNetworkId()) {
            throw ExceptionFactory.cip113InvalidAddress(
                    "Address network does not match requested network '%s'".formatted(network.getName()));
        }

        byte[] userCredential = resolveUserCredential(address, configuredScriptHash);
        String smartWalletAddress = cip113AddressService.buildSmartWalletAddress(
                configuredScriptHash, userCredential, network);

        Map<String, Object> accountIdentifier = new LinkedHashMap<>();
        accountIdentifier.put("address", smartWalletAddress);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("account_identifier", accountIdentifier);

        CallResponse response = new CallResponse();
        response.setResult(result);
        response.setIdempotent(true);
        return response;
    }

    private static String extractAddressParameter(@NotNull Map<String, Object> parameters) {
        Object addressValue = parameters.get("address");
        if (!(addressValue instanceof String address) || address.isBlank()) {
            throw ExceptionFactory.callParameterInvalid(
                    "Parameter 'address' must be a non-empty string");
        }

        return address;
    }

    private static byte[] resolveUserCredential(Address address, byte[] configuredScriptHash) {
        return switch (address.getAddressType()) {
            case Enterprise -> resolveEnterpriseCredential(address);
            case Base -> resolveBaseCredential(address, configuredScriptHash);
            case Ptr -> throw ExceptionFactory.cip113AddressTypeNotSupported(
                    "Pointer addresses are not supported");
            case Reward -> throw ExceptionFactory.cip113AddressTypeNotSupported(
                    "Reward addresses are not supported");
            // Rejected before prefix validation; retained so the switch stays exhaustive.
            case Byron -> throw ExceptionFactory.cip113AddressTypeNotSupported(
                    "Byron addresses are not supported");
        };
    }

    private static byte[] resolveEnterpriseCredential(Address address) {
        requireAddressLength(address, ENTERPRISE_ADDRESS_LENGTH,
                "Enterprise address is missing its payment credential");

        if (address.isScriptHashInPaymentPart()) {
            throw ExceptionFactory.cip113AddressNotSmartWallet(
                    "Enterprise script payment credentials are not supported");
        }
        if (!address.isPubKeyHashInPaymentPart()) {
            throw ExceptionFactory.cip113AddressTypeNotSupported(
                    "Enterprise address does not contain a key payment credential");
        }

        return address.getPaymentCredentialHash()
                .orElseThrow(() -> ExceptionFactory.cip113AddressTypeNotSupported(
                        "Enterprise address is missing its payment credential"));
    }

    private static byte[] resolveBaseCredential(Address address, byte[] configuredScriptHash) {
        requireAddressLength(address, BASE_ADDRESS_LENGTH,
                "Base address is missing a payment or stake credential");

        if (!address.isStakeKeyHashInDelegationPart()) {
            throw ExceptionFactory.cip113AddressTypeNotSupported(
                    "Base addresses must contain a key stake credential");
        }

        byte[] stakeCredential = address.getDelegationCredentialHash()
                .orElseThrow(() -> ExceptionFactory.cip113AddressTypeNotSupported(
                        "Base address is missing its stake credential"));

        if (address.isPubKeyHashInPaymentPart()) {
            return stakeCredential;
        }
        if (!address.isScriptHashInPaymentPart()) {
            throw ExceptionFactory.cip113AddressTypeNotSupported(
                    "Base address has an unknown payment credential type");
        }

        byte[] paymentScriptHash = address.getPaymentCredentialHash()
                .orElseThrow(() -> ExceptionFactory.cip113AddressTypeNotSupported(
                        "Base address is missing its payment credential"));
        if (!Arrays.equals(paymentScriptHash, configuredScriptHash)) {
            throw ExceptionFactory.cip113AddressNotSmartWallet(
                    "Payment script hash does not match configured CIP113_BASE_SCRIPT_HASH");
        }

        return stakeCredential;
    }

    private static void requireAddressLength(Address address, int expectedLength, String details) {
        int actualLength = address.getBytes().length;
        if (actualLength < expectedLength) {
            throw ExceptionFactory.cip113InvalidAddress(details);
        }
        if (actualLength > expectedLength) {
            throw ExceptionFactory.cip113InvalidAddress(
                    "Address payload contains trailing bytes");
        }
    }

    private static void requireCanonicalPrefix(Address address) {
        String expectedPrefix = AddressEncoderDecoderUtil.getPrefixHeader(address.getAddressType())
                + AddressEncoderDecoderUtil.getPrefixTail(
                AddressEncoderDecoderUtil.getNetworkId(address.getNetwork()));

        if (!expectedPrefix.equals(address.getPrefix())) {
            throw ExceptionFactory.cip113InvalidAddress(
                    "Address prefix does not match its type or network");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CallResponse getParseErrorBlocks(@Nullable ReviewStatus status) {
        log.info("Getting parse error blocks with status filter: {}", status);
        
        List<BlockParsingErrorReviewDTO> errorBlocks = blockParsingErrorReviewService.findTop1000(status);
        
        // Build the response according to the API specification
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> parseErrorBlocks = errorBlocks.stream()
                .map(this::mapToParseErrorBlock)
                .toList();
        
        result.put("parse_error_blocks", parseErrorBlocks);
        
        CallResponse response = new CallResponse();
        response.setResult(result);
        response.setIdempotent(false);
        
        log.info("Returning {} parse error blocks", parseErrorBlocks.size());

        return response;
    }

    private Optional<ReviewStatus> extractStatusParameter(Map<String, Object> parameters) {
        if (parameters == null) {
            throw ExceptionFactory.callParameterMissing("Parameters cannot be null");
        }

        Object statusValue = parameters.get("status");
        if (statusValue instanceof String statusStr) {
            try {
                return Optional.of(ReviewStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status parameter: {}", statusStr);

                throw ExceptionFactory.callParameterMissing(String.format("'status' parameter must be one of: %s",
                        Arrays.toString(ReviewStatus.values())));
            }
        }

        return Optional.empty();
    }
    
    @Override
    @Transactional(readOnly = false) // Explicitly allow writes
    public CallResponse markParseErrorBlockChecked(Map<String, Object> params) {
        if (params == null) {
            throw ExceptionFactory.callParameterMissing("Parameters cannot be null");
        }

        Long blockNumber = extractBlockNumber(params);
        ReviewStatus reviewStatus = extractStatusParameter(params)
                .orElseThrow(() -> ExceptionFactory.callParameterMissing("'review_status' parameter is required for mark_checked operation"));

        // Only allow reviewed statuses for mark_checked operation
        if (reviewStatus == UNREVIEWED) {
            throw ExceptionFactory.invalidBlockErrorReviewStatus();
        }
        
        String checkedBy = (String) params.get("checked_by");
        String comment = (String) params.get("comment");
        
        log.info("Marking parse error block {} as checked with reviewStatus: {}", blockNumber, reviewStatus);
        
        // Find all errors for this block number
        List<BlockParsingErrorReviewDTO> errorBlocks = blockParsingErrorReviewService.findTop1000ByBlockNumber(blockNumber);

        log.info("Found {} errors for block {}", errorBlocks.size(), blockNumber);

        List<Map<String, Object>> errors = errorBlocks.stream()
                .map(err -> blockParsingErrorReviewService.upsert(err.id(), reviewStatus, comment, checkedBy))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(errorReviewEntity -> mapParseErrorBlockChecked(blockNumber, errorReviewEntity))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("parse_error_blocks_response", errors);

        CallResponse response = new CallResponse();
        response.setResult(result);
        response.setIdempotent(true);
        
        log.info("Updated {} errors for block {}", errors.size(), blockNumber);

        return response;
    }

    private static Long extractBlockNumber(Map<String, Object> params) {
        Object blockNumberObj = params.get("block_number");
        if (blockNumberObj == null) {
            throw ExceptionFactory.callParameterMissing("block_number parameter is required");
        }

        if (blockNumberObj instanceof Number number) {
            return number.longValue();
        }

        throw ExceptionFactory.callParameterMissing("block_number must be a number");
    }

    private Map<String, Object> mapToParseErrorBlock(BlockParsingErrorReviewDTO dto) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("error_id", dto.id());
        block.put("block_number", dto.block());
        block.put("status", dto.status().name());
        if (dto.comment() != null) {
            block.put("comment", dto.comment());
        }
        if (dto.checkedBy() != null) {
            block.put("checked_by", dto.checkedBy());
        }
        block.put("lastUpdated", dto.lastUpdated());
        block.put("note", dto.note());

        return block;
    }

    private Map<String, Object> mapParseErrorBlockChecked(long blockNumber,
                                                          ErrorReviewEntity errorReviewEntity) {
        Map<String, Object> answerMap = new LinkedHashMap<>();
        answerMap.put("error_id", errorReviewEntity.getId());
        answerMap.put("block_number", blockNumber);

        answerMap.put("status", errorReviewEntity.getStatus());
        if (errorReviewEntity.getComment() != null) {
            answerMap.put("comment", errorReviewEntity.getComment());
        }
        if (errorReviewEntity.getCheckedBy() != null) {
            answerMap.put("checked_by", errorReviewEntity.getCheckedBy());
        }
        answerMap.put("lastUpdated", errorReviewEntity.getLastUpdated());

        return answerMap;
    }

}
