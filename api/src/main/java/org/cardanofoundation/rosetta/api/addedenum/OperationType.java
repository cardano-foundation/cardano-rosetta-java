package org.cardanofoundation.rosetta.api.addedenum;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OperationType {
    INPUT ( "input"),
    OUTPUT ( "output"),
    STAKE_KEY_REGISTRATION ( "stakeKeyRegistration"),
    STAKE_DELEGATION ( "stakeDelegation"),
    WITHDRAWAL ( "withdrawal"),
    STAKE_KEY_DEREGISTRATION ( "stakeKeyDeregistration"),
    POOL_REGISTRATION ( "poolRegistration"),
    POOL_REGISTRATION_WITH_CERT ( "poolRegistrationWithCert"),
    POOL_RETIREMENT ( "poolRetirement"),
    VOTE_REGISTRATION ( "voteRegistration");

    private String value;

    OperationType(String value) {
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
