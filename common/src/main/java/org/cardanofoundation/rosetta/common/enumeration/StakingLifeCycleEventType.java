package org.cardanofoundation.rosetta.common.enumeration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum StakingLifeCycleEventType {
    REGISTRATION(0),
    DELEGATION(1),
    REWARDS(2),
    WITHDRAWAL(3),
    DEREGISTRATION(4);

    int value;
}
