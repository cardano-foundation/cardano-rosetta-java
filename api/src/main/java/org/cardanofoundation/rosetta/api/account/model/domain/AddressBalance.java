package org.cardanofoundation.rosetta.api.account.model.domain;

import java.math.BigInteger;

import lombok.Builder;
import org.cardanofoundation.rosetta.common.util.Constants;

import javax.annotation.Nullable;

@Builder
public record AddressBalance(String address,
                             String unit,
                             Long slot,
                             BigInteger quantity,
                             Long number) {

    /**
     * Returns symbol as hex
     * unit (subject) = policyId(hex) + symbol(hex)
     */
    @Nullable
    public String getSymbol() {
        if (unit == null || unit.length() < Constants.POLICY_ID_LENGTH) {
            return null;
        }

        return unit.substring(Constants.POLICY_ID_LENGTH);
    }

    /**
     * Returns policyId as hex
     * unit (subject) = policyId(hex) + symbol(hex)
     */
    @Nullable
    public String getPolicyId() {
        if (unit == null || unit.length() < Constants.POLICY_ID_LENGTH) {
            return null;
        }

        return unit.substring(0, Constants.POLICY_ID_LENGTH);
    }

}
