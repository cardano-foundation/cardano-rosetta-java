package org.cardanofoundation.rosetta.testgenerator.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeneratedTestDataDTO {

  /**
   * Transaction Hash where the TestAddress was Toped Up with 2 Ada
   */
  private String topUpTxHash;
  /**
   * Block Hash where the TestAddress was Toped Up with 2 Ada
   */
  private String topUpBlockHash;
  /**
   * Block Slot where the TestAddress was Toped Up with 2 Ada
   */
  private long topUpBlockNumber;


  private String stakeKeyRegistrationTxHash;
  private String stakeKeyRegistrationBlockHash;
  private long stakeKeyRegistrationBlockSlot;

  private String stakeKeyDeregistrationTxHash;
  private String stakeKeyDeregistrationBlockHash;
  private long stakeKeyDeregistrationBlockNumber;

  private String withdrawalTxHash;
  private String withdrawalBlockHash;
  private long withdrawalBlockNumber;

  private String poolRegistrationTxHash;
  private String poolRegistrationBlockHash;
  private long poolRegistrationBlockNumber;


  private String poolRetireTxHash;
  private String poolRetireBlockHash;
  private long poolRetireBlockNumber;

  private String delegationTxHash;
  private String delegationBlockHash;
  private long delegationBlockNumber;

}
