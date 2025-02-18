package org.cardanofoundation.rosetta.api.construction.service;

import org.openapitools.client.model.ProtocolParameters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;

import static org.assertj.core.api.Assertions.assertThat;

class ProtocolParamsConverterImplTest {

    private ProtocolParamsConverterImpl protocolParamsMapper;

    @BeforeEach
    void setUp() {
        protocolParamsMapper = new ProtocolParamsConverterImpl();
    }

    @Test
    void shouldConvertProtocolParametersToProtocolParams() {
        ProtocolParameters protocolParameters = new ProtocolParameters();
        protocolParameters.setMinFeeCoefficient(100);
        protocolParameters.setMinFeeConstant(200);
        protocolParameters.setMaxTxSize(300);

        ProtocolParams protocolParams = protocolParamsMapper.convert(protocolParameters);

        assertThat(protocolParams).isNotNull();
        assertThat(protocolParams.getMinFeeA()).isEqualTo(100);
        assertThat(protocolParams.getMinFeeB()).isEqualTo(200);
        assertThat(protocolParams.getMaxTxSize()).isEqualTo(300);
    }

}
