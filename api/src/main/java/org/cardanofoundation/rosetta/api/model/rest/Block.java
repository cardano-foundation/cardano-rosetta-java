package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
public class Block {
    @JsonProperty("block_identifier")
    private BlockIdentifier blockIdentifier;

    @JsonProperty("parent_block_identifier")
    private BlockIdentifier parentBlockIdentifier;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("transactions")
    @Valid
    private List<Transaction> transactions = new ArrayList<>();

    @JsonProperty("metadata")
    private Object metadata;

    public Block blockIdentifier(BlockIdentifier blockIdentifier) {
        this.blockIdentifier = blockIdentifier;
        return this;
    }

    /**
     * Get blockIdentifier
     * @return blockIdentifier
     */
    @NotNull
    @Valid
    @Schema(name = "block_identifier", requiredMode = Schema.RequiredMode.REQUIRED)
    public BlockIdentifier getBlockIdentifier() {
        return blockIdentifier;
    }

    public void setBlockIdentifier(BlockIdentifier blockIdentifier) {
        this.blockIdentifier = blockIdentifier;
    }

    public Block parentBlockIdentifier(BlockIdentifier parentBlockIdentifier) {
        this.parentBlockIdentifier = parentBlockIdentifier;
        return this;
    }

    /**
     * Get parentBlockIdentifier
     * @return parentBlockIdentifier
     */
    @NotNull @Valid
    @Schema(name = "parent_block_identifier", requiredMode = Schema.RequiredMode.REQUIRED)
    public BlockIdentifier getParentBlockIdentifier() {
        return parentBlockIdentifier;
    }

    public void setParentBlockIdentifier(BlockIdentifier parentBlockIdentifier) {
        this.parentBlockIdentifier = parentBlockIdentifier;
    }

    public Block timestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * The timestamp of the block in milliseconds since the Unix Epoch. The timestamp is stored in milliseconds because some blockchains produce blocks more often than once a second.
     * minimum: 0
     * @return timestamp
     */
    @NotNull @Min(0L)
    @Schema(name = "timestamp", example = "1582833600000", description = "The timestamp of the block in milliseconds since the Unix Epoch. The timestamp is stored in milliseconds because some blockchains produce blocks more often than once a second. ", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Block transactions(List<Transaction> transactions) {
        this.transactions = transactions;
        return this;
    }

    public Block addTransactionsItem(Transaction transactionsItem) {
        this.transactions.add(transactionsItem);
        return this;
    }

    /**
     * Get transactions
     * @return transactions
     */
    @NotNull @Valid
    @Schema(name = "transactions", requiredMode = Schema.RequiredMode.REQUIRED)
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Block metadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get metadata
     * @return metadata
     */

    @Schema(name = "metadata", example = "{transactions_root=0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347, difficulty=123891724987128947}", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Block block = (Block) o;
        return Objects.equals(this.blockIdentifier, block.blockIdentifier) &&
                Objects.equals(this.parentBlockIdentifier, block.parentBlockIdentifier) &&
                Objects.equals(this.timestamp, block.timestamp) &&
                Objects.equals(this.transactions, block.transactions) &&
                Objects.equals(this.metadata, block.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockIdentifier, parentBlockIdentifier, timestamp, transactions, metadata);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Block {\n");
        sb.append("    blockIdentifier: ").append(toIndentedString(blockIdentifier)).append("\n");
        sb.append("    parentBlockIdentifier: ").append(toIndentedString(parentBlockIdentifier)).append("\n");
        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
        sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
