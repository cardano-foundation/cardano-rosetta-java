package org.cardanofoundation.rosetta.api.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CatalystDataIndexes {
    VOTING_KEY(1l),
    STAKE_KEY(2l),
    REWARD_ADDRESS(3l),
    VOTING_NONCE(4l);

    private Long value;

    CatalystDataIndexes(Long value) {
        this.value = value;
    }

    @JsonValue
    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static CatalystDataIndexes findByValue(Long value){
        for(CatalystDataIndexes a:CatalystDataIndexes.values()){
            if(a.getValue()==value) return a;
        }
        return null;
    }
}
