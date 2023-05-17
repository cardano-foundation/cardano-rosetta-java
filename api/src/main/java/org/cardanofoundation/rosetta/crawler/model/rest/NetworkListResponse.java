package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:33
 */
@Getter
public class NetworkListResponse {
    @JsonProperty("network_identifiers")
    private List<NetworkIdentifier> networkIdentifiers = new ArrayList<>();

    public void addNetworkIdentifiersItem(NetworkIdentifier identifier) {
        networkIdentifiers.add(identifier);
    }
}
