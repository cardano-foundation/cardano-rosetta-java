package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.model.entity.ProtocolParams;

import java.io.FileNotFoundException;

public interface CardanoConfigService {
    ProtocolParams getProtocolParameters() throws FileNotFoundException;
}
