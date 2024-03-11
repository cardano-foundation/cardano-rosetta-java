package org.cardanofoundation.rosetta.common.services;


import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;

import java.io.FileNotFoundException;

public interface CardanoConfigService {
    ProtocolParams getProtocolParameters() throws FileNotFoundException;
}
