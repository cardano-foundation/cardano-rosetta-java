package org.cardanofoundation.rosetta.api.construction.data.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StakeAddressPrefix {
    MAIN("stake"),
    TEST("stake_test");

    private String value;

    StakeAddressPrefix(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static StakeAddressPrefix findByValue(String value){
        for(StakeAddressPrefix a:StakeAddressPrefix.values()){
            if(a.getValue().equals(value)) return a;
        }
        return null;
    }
}
