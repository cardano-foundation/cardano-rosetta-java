package org.cardanofoundation.rosetta.api.construction.service;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.openapitools.client.model.ProtocolParameters;
import org.springframework.stereotype.Service;

@Service
public class ProtocolParamsConverterImpl implements ProtocolParamsConverter {

    public ProtocolParams convert(ProtocolParameters protocolParameters) {
        return ProtocolParams.builder()
                .minFeeA(protocolParameters.getMinFeeCoefficient())
                .minFeeB(protocolParameters.getMinFeeConstant())
                .maxTxSize(protocolParameters.getMaxTxSize())
                .build();
    }

}
