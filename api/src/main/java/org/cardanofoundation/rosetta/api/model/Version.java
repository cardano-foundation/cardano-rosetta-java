package org.cardanofoundation.rosetta.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:55
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Version {
  private String rosettaVersion;
  private String nodeVersion;
  private String middlewareVersion;
  private Object metadata;
}
