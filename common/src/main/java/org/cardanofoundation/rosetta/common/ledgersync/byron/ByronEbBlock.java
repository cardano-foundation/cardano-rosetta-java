package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.kafka.AbstractBlock;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronEbBlock extends AbstractBlock implements ByronBlock {

    public static final String TYPE = "ByronEbBlock";

    private ByronEbHead header;
    private ByronEbBody body;

    @Override
    public String getType() {
        return TYPE;
    }


    @Override
    public String getBlockHash() {
        return header.getBlockHash();
    }

    @Override
    public long getSlot() {
        return header.getConsensusData().getEpochId() * 21600;
    }

    @Override
    public long getBlockNumber() {
        return 0;
    }

    @Override
    public String getPreviousHash() {
        return header.getPrevBlock();
    }

    public ByronEbHead getHeader() {
        return header;
    }
}
