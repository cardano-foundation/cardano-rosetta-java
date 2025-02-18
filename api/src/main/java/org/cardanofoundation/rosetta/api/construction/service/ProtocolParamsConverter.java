package org.cardanofoundation.rosetta.api.construction.service;

import org.openapitools.client.model.ProtocolParameters;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;

public interface ProtocolParamsConverter {

    ProtocolParams convert(ProtocolParameters protocolParameters);

}
