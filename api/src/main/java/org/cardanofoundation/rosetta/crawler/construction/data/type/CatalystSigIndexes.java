package org.cardanofoundation.rosetta.crawler.construction.data.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CatalystSigIndexes {
    VOTING_SIGNATURE(1);

    private int value;

    CatalystSigIndexes(int value) {
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

    public static CatalystSigIndexes findByValue(int value){
        for(CatalystSigIndexes a:CatalystSigIndexes.values()){
            if(a.getValue()==value) return a;
        }
        return null;
    }
}
