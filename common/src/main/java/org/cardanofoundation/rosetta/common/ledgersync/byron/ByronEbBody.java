package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronEbBody {
  private List<String> stakeholderIds;
}
