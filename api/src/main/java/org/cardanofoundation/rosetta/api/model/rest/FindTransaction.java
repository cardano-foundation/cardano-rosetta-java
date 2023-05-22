package org.cardanofoundation.rosetta.api.model.rest;

import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindTransaction {

  private ByteBuffer hash;
  private ByteBuffer blockHash;
  private int blockNo;
  private String fee;
  private int size;
  private int scriptSize;
  private boolean validContract;
}