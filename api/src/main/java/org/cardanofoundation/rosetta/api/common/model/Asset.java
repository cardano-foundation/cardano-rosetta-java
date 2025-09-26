package org.cardanofoundation.rosetta.api.common.model;

import com.bloxbean.cardano.client.util.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.charset.StandardCharsets;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class Asset {

    private final String policyId;
    private final String assetName;
    
    public String toSubject() {
        return policyId + HexUtil.encodeHexString(assetName.getBytes(StandardCharsets.UTF_8));
    }

}