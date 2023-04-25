package org.cardanofoundation.rosetta.api.addedClass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddedNetWork {
    String networkId;
    Integer networkMagic;

    public AddedNetWork(String networkId) {
        this.networkId = networkId;
    }
}
