package org.cardanofoundation.rosetta.api.model.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UtxoKey implements Serializable {
    private String txHash;
    private Integer outputIndex;
}
