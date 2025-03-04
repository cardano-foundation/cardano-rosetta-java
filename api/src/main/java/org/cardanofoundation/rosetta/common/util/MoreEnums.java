package org.cardanofoundation.rosetta.common.util;

import java.util.Arrays;
import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MoreEnums {

    // Generic method to retrieve an enum value by its string representation.
    public <T extends Enum<T>> Optional<T> getIfPresent(Class<T> enumClass, String value) {
        if (value == null) {
            return Optional.empty();
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .filter(enumConstant -> enumConstant.name().equalsIgnoreCase(value))
                .findFirst();
    }

}
