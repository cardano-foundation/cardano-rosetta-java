package org.cardanofoundation.rosetta.api.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.common.entity.Block;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockDto {

  private String hash;
  private Long number;
  private Long createdAt;
  private String previousBlockHash;
  private Long previousBlockNumber;
  private Long transactionsCount;
  private String createdBy;
  private Integer size;
  private Integer epochNo;
  private Long slotNo;

  public static BlockDto fromBlock(Block block) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date parse;
      try {
        parse = dateFormat.parse(block.getCreateDatetime().toString());
      } catch (ParseException e) {
          throw new RuntimeException(e);
      }
      return BlockDto.builder()
            .number(block.getNumber())
            .hash(block.getHash())
            .createdAt(parse.getTime())
            .previousBlockHash(
                    block.getPrevious().getHash())
            .previousBlockNumber(block.getPrevious().getNumber())
            .transactionsCount(block.getNoOfTxs())
            .size(block.getBodySize())
            .epochNo(block.getEpoch())
            .slotNo(block.getSlot())
            .build();
  }
}
