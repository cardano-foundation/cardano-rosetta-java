package org.cardanofoundation.rosetta.api.construction.service;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.openapitools.client.model.ProtocolParameters;

public interface ProtocolParamsConverter {

    ProtocolParams convert(ProtocolParameters protocolParameters);

}
