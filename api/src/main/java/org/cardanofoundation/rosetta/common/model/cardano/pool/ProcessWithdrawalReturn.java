package org.cardanofoundation.rosetta.common.model.cardano.pool;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.bloxbean.cardano.client.address.Address;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProcessWithdrawalReturn {

    // reward address holding the rewards
    private Address reward;
    // address to which the rewards will be withdrawn
    private String address;

}
