package org.cardanofoundation.rosetta.api.network.service;

import java.io.IOException;
import java.util.Map;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.FileUtils;

import static org.cardanofoundation.rosetta.common.util.Constants.NETWORK_MAGIC_NAME;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenesisDataProviderImpl implements GenesisDataProvider {

    @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
    private String genesisShelleyPath;

    private int protocolMagic;

    @PostConstruct
    public void init() {
        Map<String, Object> jsonMap = loadGenesisShelleyConfig();
        protocolMagic = (Integer) jsonMap.get(NETWORK_MAGIC_NAME);

        log.info("Protocol magic number: {} loaded from genesis config json", protocolMagic);
    }

    @Override
    public int getProtocolMagic() {
        return protocolMagic;
    }

    private Map<String, Object> loadGenesisShelleyConfig() {
        try {
            String content = FileUtils.fileReader(genesisShelleyPath);

            return new ObjectMapper().readValue(content, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw ExceptionFactory.configNotFoundException(genesisShelleyPath);
        }
    }

}
