package org.cardanofoundation.rosetta.api.addedenum;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CatalystDataIndexes {
    VOTING_KEY(1),
    STAKE_KEY(2),
    REWARD_ADDRESS(3),
    VOTING_NONCE(4);

    private int value;

    CatalystDataIndexes(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static CatalystDataIndexes findByValue(int value){
        for(CatalystDataIndexes a:CatalystDataIndexes.values()){
            if(a.getValue()==value) return a;
        }
        return null;
    }
}
