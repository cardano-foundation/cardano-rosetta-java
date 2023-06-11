package org.cardanofoundation.rosetta.consumer.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.common.entity.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenesisData {

  List<Block> blocks;
  List<Tx> txs;
  List<TxOut> txOuts;
  List<SlotLeader> slotLeaders;
  CostModel costModel;
  EpochParam shelley;
  EpochParam alonzo;
}
