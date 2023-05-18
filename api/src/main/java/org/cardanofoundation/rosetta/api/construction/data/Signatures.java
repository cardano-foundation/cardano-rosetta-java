package org.cardanofoundation.rosetta.api.construction.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Signatures {
    private String signature;
    private String publicKey;
    private String chainCode;
    private String address;
}
