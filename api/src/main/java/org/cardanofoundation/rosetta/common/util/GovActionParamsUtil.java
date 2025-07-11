package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

import java.util.regex.Pattern;

@Slf4j
public class GovActionParamsUtil {

    private static final int TX_HASH_LENGTH = 64;
    private static final int INDEX_LENGTH = 2;
    private static final int TOTAL_LENGTH = TX_HASH_LENGTH + INDEX_LENGTH;
    private static final int MAX_INDEX_DECIMAL = 99;
    
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");
    
    @Getter
    public static class ParsedGovActionParams {

        private final String txId;
        private final int index;

        public ParsedGovActionParams(String txId, int index) {
            this.txId = txId;
            this.index = index;
        }

        public GovActionId toGovActionId() {
            return GovActionId.builder()
                    .transactionId(txId)
                    .govActionIndex(index)
                    .build();
        }
    }

    /**
     * Validates and parses a concatenated governance action string.
     * Expected format: 64-character hex tx_id + 2-character hex index
     * 
     * @param govActionString The concatenated governance action string
     * @return ParsedGovActionParams containing the tx_id and index
     * @throws ApiException if the format is invalid
     */
    public static ParsedGovActionParams parseAndValidate(String govActionString) {
        if (govActionString == null || govActionString.trim().isEmpty()) {
            log.error("[parseAndValidate] Governance action string is null or empty");

            throw ExceptionFactory.invalidGovernanceVote("Governance action parameter is required");
        }

        String trimmed = govActionString.trim();
        
        // Check total length
        if (trimmed.length() != TOTAL_LENGTH) {
            log.error("[parseAndValidate] Invalid governance action length: {} (expected: {})", 
                trimmed.length(), TOTAL_LENGTH);
            throw ExceptionFactory.invalidGovernanceVote(
                String.format("Governance action must be exactly %d characters long (64 for tx_id + 2 for index), got %d", 
                    TOTAL_LENGTH, trimmed.length()));
        }

        // Check if it's all hex characters
        if (!HEX_PATTERN.matcher(trimmed).matches()) {
            log.error("[parseAndValidate] Governance action contains non-hex characters: {}", trimmed);

            throw ExceptionFactory.invalidGovernanceVote(
                "Governance action must contain only hexadecimal characters (0-9, a-f, A-F)");
        }

        // Split into tx_id and index
        String txId = trimmed.substring(0, TX_HASH_LENGTH);
        String indexHex = trimmed.substring(TX_HASH_LENGTH);

        // Validate and parse index
        int index;
        try {
            index = Integer.parseInt(indexHex, 16);
        } catch (NumberFormatException e) {
            log.error("[parseAndValidate] Failed to parse index as hex: {}", indexHex, e);
            throw ExceptionFactory.invalidGovernanceVote(
                String.format("Invalid index format: %s", indexHex));
        }

        // Check index range
        if (index > MAX_INDEX_DECIMAL) {
            log.error("[parseAndValidate] Index {} exceeds maximum allowed value {}", index, MAX_INDEX_DECIMAL);
            throw ExceptionFactory.invalidGovernanceVote(
                String.format("Index %d exceeds maximum allowed value %d", index, MAX_INDEX_DECIMAL));
        }

        log.debug("[parseAndValidate] Successfully parsed governance action - tx_id: {}, index: {}", txId, index);
        return new ParsedGovActionParams(txId, index);
    }

    /**
     * Formats a tx_id and index into a concatenated governance action string.
     * 
     * @param txId The transaction ID (64 hex characters)
     * @param index The index (0-99)
     * @return The concatenated governance action string
     */
    public static String formatGovActionString(String txId, int index) {
        if (txId == null || txId.length() != TX_HASH_LENGTH) {
            throw new IllegalArgumentException("Transaction ID must be exactly 64 characters");
        }
        if (index < 0 || index > MAX_INDEX_DECIMAL) {
            throw new IllegalArgumentException("Index must be between 0 and " + MAX_INDEX_DECIMAL);
        }

        return txId + String.format("%02x", index);
    }

}
