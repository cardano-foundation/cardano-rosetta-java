package org.cardanofoundation.rosetta.api.model.rest;

import lombok.*;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockIdentifier {
  private Long index;
  private String hash;
}
