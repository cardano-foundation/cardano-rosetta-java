package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.rosetta.crawler.model.ConstructionPayloadsRequestMetadata;
import org.cardanofoundation.rosetta.crawler.model.Operation;
import org.cardanofoundation.rosetta.crawler.model.PublicKey;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:15
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConstructionPayloadsRequest {
    @JsonProperty("network_identifier")
    private NetworkIdentifier networkIdentifier;

    @JsonProperty("operations")
    @Valid
    private List<Operation> operations = new ArrayList<>();

    @JsonProperty("metadata")
    private ConstructionPayloadsRequestMetadata metadata;

    @JsonProperty("public_keys")
    @Valid
    private List<PublicKey> publicKeys = null;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConstructionPayloadsRequest {\n");
        sb.append("    networkIdentifier: ").append(toIndentedString(networkIdentifier)).append("\n");
        sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    publicKeys: ").append(toIndentedString(publicKeys)).append("\n");
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
