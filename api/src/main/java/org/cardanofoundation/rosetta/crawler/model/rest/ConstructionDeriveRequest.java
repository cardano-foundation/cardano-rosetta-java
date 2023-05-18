package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.rosetta.crawler.model.ConstructionDeriveRequestMetadata;
import org.cardanofoundation.rosetta.crawler.model.PublicKey;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:13
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConstructionDeriveRequest {
    @JsonProperty("network_identifier")
    private NetworkIdentifier networkIdentifier;

    @JsonProperty("public_key")
    private PublicKey publicKey;

    @JsonProperty("metadata")
    private ConstructionDeriveRequestMetadata metadata;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConstructionDeriveRequest {\n");
        sb.append("    networkIdentifier: ").append(toIndentedString(networkIdentifier)).append("\n");
        sb.append("    publicKey: ").append(toIndentedString(publicKey)).append("\n");
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
