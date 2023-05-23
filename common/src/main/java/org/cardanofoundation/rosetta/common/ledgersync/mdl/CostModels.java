package org.cardanofoundation.rosetta.common.ledgersync.mdl;

import com.bloxbean.cardano.client.transaction.spec.Language;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CostModels {
  private String hash;
  private Map<Language, List<BigInteger>>  languages;
}
