package org.cardanofoundation.rosetta.client;

import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Gateway for interacting with the Cardano Token Registry API
 */
public interface TokenRegistryHttpGateway {

    /**
     * Get token metadata for multiple subjects using batch request
     * @param subjects Set of subject identifiers (policy_id + asset_name hex)
     * @return Map of subject -> Optional<TokenSubject> with metadata
     */
    Map<String, Optional<TokenSubject>> getTokenMetadataBatch(Set<String> subjects);

}
