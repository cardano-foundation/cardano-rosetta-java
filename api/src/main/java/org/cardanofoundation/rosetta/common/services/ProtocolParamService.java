package org.cardanofoundation.rosetta.common.services;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;

public interface ProtocolParamService {

  ProtocolParams findProtocolParameters();
}
