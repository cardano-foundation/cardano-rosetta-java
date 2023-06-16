package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:33
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NetworkListResponse {
    @JsonProperty("network_identifiers")
    private List<NetworkIdentifier> networkIdentifiers = new ArrayList<>();

    public void addNetworkIdentifiersItem(NetworkIdentifier identifier) {
        networkIdentifiers.add(identifier);
    }
}
