package org.cardanofoundation.rosetta.client.model.domain;

import java.util.Optional;

/**
 * Cache entry wrapper for token registry responses.
 * Allows us to cache both found tokens and "not found" results to avoid repeated registry calls.
 * This prevents unnecessary HTTP requests for tokens that don't exist in the registry.
 */
public record TokenCacheEntry(Optional<TokenSubject> tokenSubject, boolean found) {
    
    /**
     * Creates a cache entry for a found token
     * 
     * @param tokenSubject the token metadata found in the registry
     * @return cache entry representing a found token
     */
    public static TokenCacheEntry found(TokenSubject tokenSubject) {
        return new TokenCacheEntry(Optional.of(tokenSubject), true);
    }
    
    /**
     * Creates a cache entry for a token that was not found in the registry
     * 
     * @return cache entry representing a token not found in the registry
     */
    public static TokenCacheEntry notFound() {
        return new TokenCacheEntry(Optional.empty(), false);
    }
    
    /**
     * Returns true if this entry represents a token that was found in the registry
     * 
     * @return true if token was found and has valid metadata
     */
    public boolean isFound() {
        return found && tokenSubject.isPresent();
    }
    
    /**
     * Returns the token subject if found, otherwise empty Optional
     * 
     * @return Optional containing the TokenSubject if found, empty otherwise
     */
    public Optional<TokenSubject> getTokenSubject() {
        return tokenSubject;
    }
}