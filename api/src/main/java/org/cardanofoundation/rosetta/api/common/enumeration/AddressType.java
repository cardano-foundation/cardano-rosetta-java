package org.cardanofoundation.rosetta.api.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AddressType {
    ENTERPRISE("Enterprise"),
    BASE("Base"),
    REWARD("Reward"),
    POOL_KEY_HASH("Pool_Hash");

    private String value;

    AddressType(String value) {
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

    public static AddressType findByValue(String value){
        for(AddressType a:AddressType.values()){
            if(a.getValue().equals(value)) return a;
        }
        return null;
    }
}
