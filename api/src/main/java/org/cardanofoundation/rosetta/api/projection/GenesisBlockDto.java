package org.cardanofoundation.rosetta.api.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenesisBlockDto {
    private String hash;
    private Long number;


}
