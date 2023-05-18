package org.cardanofoundation.rosetta.crawler.projection;




public interface GenesisBlockProjection {
    byte[] getHash();
    Long getIndex();
}
