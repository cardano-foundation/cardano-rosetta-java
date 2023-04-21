package org.cardanofoundation.rosetta.crawler.model.rest;

import lombok.*;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:33
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalanceRequest {
  private NetworkIdentifier networkIdentifier;
  private AccountIdentifier accountIdentifier;
}
