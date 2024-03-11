package org.cardanofoundation.rosetta.api.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.exception.ServerException;
import org.cardanofoundation.rosetta.api.model.entity.ProtocolParams;
import org.cardanofoundation.rosetta.api.service.CardanoConfigService;
import org.cardanofoundation.rosetta.api.util.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardanoConfigServiceImpl implements CardanoConfigService {

    @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
    private String genesisShelleyPath;

    @PostConstruct
    public void filePathExistingValidator() throws ServerException {
        validator(genesisShelleyPath);
    }

    private void validator( String path) throws ServerException {
        if(!new File(path).exists()) {
            throw ExceptionFactory.configNotFoundException();
        }
    }

    @Override
    public ProtocolParams getProtocolParameters() throws FileNotFoundException {
        String shelleyContent = null;
        try {
            shelleyContent = FileUtils.fileReader(genesisShelleyPath);
        } catch (IOException e) {
            throw new FileNotFoundException("Genesis shelley file not found: " + genesisShelleyPath);
        }
        JSONObject shelleyJsonObject = new JSONObject(shelleyContent);
        return ProtocolParams.fromJSONObject(shelleyJsonObject);
    }

}
