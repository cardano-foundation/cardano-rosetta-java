package org.cardanofoundation.rosetta.common.model.cardano.network;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RelayType {
    SINGLE_HOST_ADDR("SINGLE_HOST_ADDR"),
    SINGLE_HOST_NAME("SINGLE_HOST_NAME"),

    MULTI_HOST_NAME("MULTI_HOST_NAME");
    private String value;

    RelayType(String value) {
        this.value = value;
    }
    RelayType() {
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    public static RelayType find(String network){
        for(RelayType relayType :RelayType.values()){
            if(network.equals(relayType.getValue())){
                return relayType;
            }
        }
        return null;
    }
}
