package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:13
 */
public class ConstructionDeriveResponse {
    @JsonProperty("address")
    private String address;

    @JsonProperty("account_identifier")
    private AccountIdentifier accountIdentifier;

    @JsonProperty("metadata")
    private Object metadata;

    public ConstructionDeriveResponse address(String address) {
        this.address = address;
        return this;
    }

    /**
     * [DEPRECATED by `account_identifier` in `v1.4.4`] Address in network-specific format.
     * @return address
     */

    @Schema(name = "address", description = "[DEPRECATED by `account_identifier` in `v1.4.4`] Address in network-specific format.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ConstructionDeriveResponse(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    /**
     * Get accountIdentifier
     * @return accountIdentifier
     */
    @NotNull
    @Valid
    @Schema(name = "account_identifier", requiredMode = Schema.RequiredMode.REQUIRED)
    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public ConstructionDeriveResponse metadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get metadata
     * @return metadata
     */

    @Schema(name = "metadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
        ConstructionDeriveResponse constructionDeriveResponse = (ConstructionDeriveResponse) o;
        return Objects.equals(this.address, constructionDeriveResponse.address) &&
                Objects.equals(this.accountIdentifier, constructionDeriveResponse.accountIdentifier) &&
                Objects.equals(this.metadata, constructionDeriveResponse.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, accountIdentifier, metadata);
    }

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
