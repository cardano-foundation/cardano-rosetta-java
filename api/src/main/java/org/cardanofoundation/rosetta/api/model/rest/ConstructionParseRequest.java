package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:14
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConstructionParseRequest {
    @JsonProperty("network_identifier")
    private NetworkIdentifier networkIdentifier;

    @JsonProperty("signed")
    private Boolean signed;

    @JsonProperty("transaction")
    private String transaction;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConstructionParseRequest {\n");
        sb.append("    networkIdentifier: ").append(toIndentedString(networkIdentifier)).append("\n");
        sb.append("    signed: ").append(toIndentedString(signed)).append("\n");
        sb.append("    transaction: ").append(toIndentedString(transaction)).append("\n");
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
