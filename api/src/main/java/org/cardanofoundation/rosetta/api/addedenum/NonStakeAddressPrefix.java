package org.cardanofoundation.rosetta.api.addedenum;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NonStakeAddressPrefix {
    MAIN("addr"),
    TEST("addr_test");

    private String value;

    NonStakeAddressPrefix(String value) {
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

    public static NonStakeAddressPrefix findByValue(String value){
        for(NonStakeAddressPrefix a:NonStakeAddressPrefix.values()){
            if(a.getValue().equals(value)) return a;
        }
        return null;
    }
}
