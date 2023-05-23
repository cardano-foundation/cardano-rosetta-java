package org.cardanofoundation.rosetta.api.construction.data.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CatalystLabels {
    DATA("61284"),
    SIG("61285");

    private String value;

    CatalystLabels(String value) {
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

    public static CatalystLabels findByValue(String value){
        for(CatalystLabels a:CatalystLabels.values()){
            if(a.getValue()==value) return a;
        }
        return null;
    }
}
