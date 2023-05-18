package org.cardanofoundation.rosetta.api.projection;




public interface GenesisBlockProjection {
    byte[] getHash();
    Long getIndex();
}
