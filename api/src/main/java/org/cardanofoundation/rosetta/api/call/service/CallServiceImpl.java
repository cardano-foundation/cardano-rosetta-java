package org.cardanofoundation.rosetta.api.call.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus;
import org.cardanofoundation.rosetta.api.error.model.entity.ErrorReviewEntity;
import org.cardanofoundation.rosetta.api.error.service.BlockParsingErrorReviewService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.openapitools.client.model.CallRequest;
import org.openapitools.client.model.CallResponse;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.*;

import static org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus.UNREVIEWED;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CallServiceImpl implements CallService {

    private static final String METHOD_GET_PARSE_ERROR_BLOCKS = "get_parse_error_blocks";
    private static final String METHOD_MARK_PARSE_ERROR_BLOCK_CHECKED = "mark_parse_error_block_checked";
    
    private final BlockParsingErrorReviewService blockParsingErrorReviewService;

    @Override
    public CallResponse processCallRequest(CallRequest callRequest) {
        String method = callRequest.getMethod();

        log.info("Processing call request for method: {}", method);

        return switch (method) {
            case METHOD_GET_PARSE_ERROR_BLOCKS -> getParseErrorBlocks(extractStatusParameter(callRequest.getParameters())
                    .orElse(null));
            
            case METHOD_MARK_PARSE_ERROR_BLOCK_CHECKED -> markParseErrorBlockChecked(callRequest.getParameters());

            default -> throw ExceptionFactory.callMethodNotSupported();
        };
    }

    @Override
    public CallResponse getParseErrorBlocks(@Nullable ReviewStatus status) {
        log.info("Getting parse error blocks with status filter: {}", status);
        
        List<BlockParsingErrorReviewDTO> errorBlocks = blockParsingErrorReviewService.findTop1000(status);
        
        // Build the response according to the API specification
        Map<String, Object> result = new HashMap<>();

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

                throw ExceptionFactory.callParameterMissing(String.format("Status parameter must be one of: %s",
                        Arrays.toString(ReviewStatus.values())));
            }
        }

        return Optional.empty();
    }
    
    @Override
    @Transactional // This method modifies data, so remove readOnly
    @Modifying
    public CallResponse markParseErrorBlockChecked(Map<String, Object> params) {
        if (params == null) {
            throw ExceptionFactory.callParameterMissing("Parameters cannot be null");
        }

        Long blockNumber = extractBlockNumber(params);
        ReviewStatus reviewStatus = extractStatusParameter(params)
                .orElseThrow(() -> ExceptionFactory.callParameterMissing("reviewStatus parameter is required for mark_checked operation"));

        // Only allow reviewed statuses for mark_checked operation
        if (reviewStatus == UNREVIEWED) {
            throw ExceptionFactory.invalidBlockErrorReviewStatus();
        }
        
        String checkedBy = (String) params.get("checked_by");
        String comment = (String) params.get("comment");
        
        log.info("Marking parse error block {} as checked with reviewStatus: {}", blockNumber, reviewStatus);
        
        // Find all errors for this block number
        List<BlockParsingErrorReviewDTO> errorBlocks = blockParsingErrorReviewService.findTop1000ByBlockNumber(blockNumber);

        log.info("Found {} errors for block {}", errorBlocks, blockNumber);

        List<Map<String, Object>> errors = errorBlocks.stream()
                .map(err -> blockParsingErrorReviewService.upsert(err.id(), reviewStatus, comment, checkedBy))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(errorReviewEntity -> mapParseErrorBlockChecked(blockNumber, errorReviewEntity))
                .toList();

        Map<String, Object> result = new HashMap<>();
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
