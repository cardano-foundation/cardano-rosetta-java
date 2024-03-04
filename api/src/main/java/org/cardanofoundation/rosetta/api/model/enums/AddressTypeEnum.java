package org.cardanofoundation.rosetta.api.model.enums;

public enum AddressTypeEnum {
    BASE("Base"),
    REWARD("Reward"),
    ENTERPRISE("Enterprise");

    private final String value;

    AddressTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AddressTypeEnum fromValue(String value) {
        for (AddressTypeEnum b : AddressTypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        return BASE;
    }
}
