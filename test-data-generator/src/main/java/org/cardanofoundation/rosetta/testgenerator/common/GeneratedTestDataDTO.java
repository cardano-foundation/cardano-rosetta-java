package org.cardanofoundation.rosetta.testgenerator.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeneratedTestDataDTO {

  private String txHashTopUp;
  private String blockHashTopUp;
  private long blockHeightTopUp;

  private String stakeKeyRegistrationTxHash;
  private String stakeKeyRegistrationBlockHash;
  private long stakeKeyRegistrationBlockHeight;

  private String stakeKeyDeregistrationTxHash;
  private String stakeKeyDeregistrationBlockHash;
  private long stakeKeyDeregistrationBlockHeight;

  private String withdrawalTxHash;
  private String withdrawalBlockHash;
  private long withdrawalBlockHeight;

}
