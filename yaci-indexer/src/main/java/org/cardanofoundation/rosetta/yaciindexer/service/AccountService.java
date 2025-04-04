package org.cardanofoundation.rosetta.yaciindexer.service;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.yaciindexer.domain.model.StakeAccountRewardInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface AccountService {

    Optional<StakeAccountRewardInfo> getAccountInfo(String stakeAddress);

    @Service
    //@ConditionalOnMissingBean(AccountService.class)
    @ConditionalOnExpression("'${store.cardano.n2c-node-socket-path:}' == '' || '${store.cardano.n2c-host:}' == ''")
    @Slf4j
    class Dummy implements AccountService {

        Dummy() {
            log.info("Dummy AccountServiceImpl initialized...");
        }


        @Override
        public Optional<StakeAccountRewardInfo> getAccountInfo(String stakeAddress) {
            return Optional.empty();
        }
    }

}
