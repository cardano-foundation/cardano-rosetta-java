package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.kafka.AbstractBlock;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronMainBlock extends AbstractBlock implements ByronBlock {

    public static final String TYPE = "ByronMainBlock";

    private ByronBlockHead header;
    private ByronMainBody body;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getBlockHash() {
        return getHeader().getBlockHash();
    }

    @Override
    public long getSlot() {
        return getHeader().getConsensusData().getSlotId().getSlotId();
    }

    @Override
    public long getBlockNumber() {
        return getHeader().getConsensusData().getDifficulty().longValue();
    }

    @Override
    public String getPreviousHash() {
        return getHeader().getPrevBlock();
    }


    public ByronBlockHead getHeader() {
        return header;
    }
}
