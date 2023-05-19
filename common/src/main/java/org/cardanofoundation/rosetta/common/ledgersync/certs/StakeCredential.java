package org.cardanofoundation.rosetta.common.ledgersync.certs;

import org.cardanofoundation.rosetta.common.ledgersync.kafka.MapKey;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StakeCredential extends MapKey {
    private StakeCredentialType stakeType;
    private String hash;

    public StakeCredential(String stakeType, String hashBytes) {
        this.stakeType = StakeCredentialType.valueOf(stakeType);
        this.hash = hashBytes;
    }

}
