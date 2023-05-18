package org.cardanofoundation.rosetta.common.ledgersync;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class VrfCert {
    private String v1;
    private String v2;
}
