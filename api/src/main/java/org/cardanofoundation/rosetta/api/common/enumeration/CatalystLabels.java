package org.cardanofoundation.rosetta.api.common.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CatalystLabels {
    DATA("61284"),
    SIG("61285");

    private String label;

    CatalystLabels(String value) {
        this.label = value;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return String.valueOf(label);
    }

    public static CatalystLabels findByValue(String label){
        for(CatalystLabels a:CatalystLabels.values()){
            if(a.getLabel()==label) return a;
        }
        return null;
    }
}
