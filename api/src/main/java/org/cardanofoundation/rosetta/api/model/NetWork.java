package org.cardanofoundation.rosetta.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NetWork {
    String networkId;
    Integer networkMagic;

    public NetWork(String networkId) {
        this.networkId = networkId;
    }
}
