package org.cardanofoundation.rosetta.common.ledgersync;

import org.cardanofoundation.rosetta.common.ledgersync.kafka.AbstractBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Builder
public class RollbackBlock extends AbstractBlock {

  public static final String TYPE = "Rollback Block";

  private String blockRollbackHash;

  private long blockNo;
  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public String getBlockHash() {
    return blockRollbackHash;
  }

  @Override
  public long getSlot() {
    return -1;
  }

  @Override
  public long getBlockNumber() {
    return blockNo;
  }

  @Override
  public String getPreviousHash() {
    return null;
  }

  @Override
  public Era getEraType(){
    return Era.ROLLBACK;
  }

}
