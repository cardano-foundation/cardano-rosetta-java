package org.cardanofoundation.rosetta.api.model.rosetta;

import com.bloxbean.cardano.yaci.core.model.Datum;
import lombok.*;
import org.openapitools.client.model.Amount;

import java.util.List;

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
