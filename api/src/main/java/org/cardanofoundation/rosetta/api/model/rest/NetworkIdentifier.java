package org.cardanofoundation.rosetta.api.model.rest;

import lombok.*;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 15:08
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkIdentifier {
  private String blockchain;
  private String network;
}
