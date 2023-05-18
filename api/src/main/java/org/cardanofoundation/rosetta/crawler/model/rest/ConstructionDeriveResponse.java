package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:13
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConstructionDeriveResponse {
    @JsonProperty("address")
    private String address;

    @JsonProperty("account_identifier")
    private AccountIdentifier accountIdentifier;

    public ConstructionDeriveResponse(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    @JsonProperty("metadata")
    private Object metadata;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConstructionDeriveResponse {\n");
        sb.append("    address: ").append(toIndentedString(address)).append("\n");
        sb.append("    accountIdentifier: ").append(toIndentedString(accountIdentifier)).append("\n");
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
