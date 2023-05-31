package org.cardanofoundation.rosetta.api.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RelayType {
    SINGLE_HOST_ADDR("single_host_addr"),
    SINGLE_HOST_NAME("single_host_name"),

    MULTI_HOST_NAME("multi_host_name");
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
