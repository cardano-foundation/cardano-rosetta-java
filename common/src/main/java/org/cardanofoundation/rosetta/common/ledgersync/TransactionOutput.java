package org.cardanofoundation.rosetta.common.ledgersync;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class TransactionOutput {

  private Integer index;
  private String address;
  private List<Amount> amounts;
  private String datumHash;
  private Datum inlineDatum;
  private String scriptRef;

}
