package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.cardanofoundation.rosetta.api.model.ConstructionDeriveRequestMetadata;
import org.cardanofoundation.rosetta.api.model.PublicKey;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:13
 */
public class ConstructionDeriveRequest {
    @JsonProperty("network_identifier")
    private NetworkIdentifier networkIdentifier;

    @JsonProperty("public_key")
    private PublicKey publicKey;

    @JsonProperty("metadata")
    private ConstructionDeriveRequestMetadata metadata;

    public ConstructionDeriveRequest networkIdentifier(NetworkIdentifier networkIdentifier) {
        this.networkIdentifier = networkIdentifier;
        return this;
    }

    /**
     * Get networkIdentifier
     * @return networkIdentifier
     */
    @NotNull
    @Valid
    @Schema(name = "network_identifier", requiredMode = Schema.RequiredMode.REQUIRED)
    public NetworkIdentifier getNetworkIdentifier() {
        return networkIdentifier;
    }

    public void setNetworkIdentifier(NetworkIdentifier networkIdentifier) {
        this.networkIdentifier = networkIdentifier;
    }

    public ConstructionDeriveRequest publicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    /**
     * Get publicKey
     * @return publicKey
     */
    @NotNull @Valid
    @Schema(name = "public_key", requiredMode = Schema.RequiredMode.REQUIRED)
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public ConstructionDeriveRequest metadata(ConstructionDeriveRequestMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get metadata
     * @return metadata
     */
    @Valid
    @Schema(name = "metadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    public ConstructionDeriveRequestMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ConstructionDeriveRequestMetadata metadata) {
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
        ConstructionDeriveRequest constructionDeriveRequest = (ConstructionDeriveRequest) o;
        return Objects.equals(this.networkIdentifier, constructionDeriveRequest.networkIdentifier) &&
                Objects.equals(this.publicKey, constructionDeriveRequest.publicKey) &&
                Objects.equals(this.metadata, constructionDeriveRequest.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(networkIdentifier, publicKey, metadata);
    }

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
