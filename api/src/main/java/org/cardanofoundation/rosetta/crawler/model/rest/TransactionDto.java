package org.cardanofoundation.rosetta.crawler.model.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransactionDto {

  protected String hash;
  protected String blockHash;
  protected Long blockNo;
  protected String fee;
  protected Long size;
  protected Boolean validContract;
  protected Long scriptSize;
}