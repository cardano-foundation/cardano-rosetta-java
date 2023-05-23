package org.cardanofoundation.rosetta.consumer.dto;

import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.CostModel;
import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenesisData {

  List<Block> blocks;
  List<Tx> txs;
  List<TxOut> txOuts;
  List<SlotLeader> slotLeaders;

  CostModel costModel;
}
