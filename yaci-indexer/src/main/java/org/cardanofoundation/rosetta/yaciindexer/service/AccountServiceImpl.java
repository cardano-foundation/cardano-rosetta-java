package org.cardanofoundation.rosetta.yaciindexer.service;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import jakarta.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.yaci.core.protocol.localstate.api.Era;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.DelegationsAndRewardAccountsQuery;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.DelegationsAndRewardAccountsResult;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.store.core.service.local.LocalClientProviderManager;
import reactor.core.publisher.Mono;

import org.cardanofoundation.rosetta.yaciindexer.domain.model.StakeAccountRewardInfo;

@Service
@Slf4j
@ConditionalOnExpression("'${store.cardano.n2c-node-socket-path:}' != '' || '${store.cardano.n2c-host:}' != ''")
public class AccountServiceImpl implements AccountService {

    private final LocalClientProviderManager localClientProviderManager;

    public AccountServiceImpl(@Nullable LocalClientProviderManager localClientProviderManager) {
        this.localClientProviderManager = localClientProviderManager;
    }

    @Override
    public Optional<StakeAccountRewardInfo> getAccountInfo(String stakeAddress) {
        if (localClientProviderManager == null) {
            throw new IllegalStateException("LocalClientProvider is not initialized. Please check n2c configuration.");
        }

        Optional<LocalClientProvider> localClientProvider = localClientProviderManager.getLocalClientProvider();
        try {
            var localStateQueryClient = localClientProvider.map(LocalClientProvider::getLocalStateQueryClient).orElse(null);

            if (localStateQueryClient == null) {
                log.info("LocalStateQueryClient is not initialized. Please check if n2c-node-socket-path or n2c-host is configured properly.");

                return Optional.empty();
            }

            Address address = new Address(stakeAddress);

            try {
                localStateQueryClient.release().block(Duration.ofSeconds(5));
            } catch (Exception e) {
                //Ignore the error
            }

            Mono<DelegationsAndRewardAccountsResult> mono =
                    localStateQueryClient.executeQuery(new DelegationsAndRewardAccountsQuery(Era.Conway, Set.of(address)));

            DelegationsAndRewardAccountsResult delegationsAndRewardAccountsResult = mono.block(Duration.ofSeconds(5));

            if (delegationsAndRewardAccountsResult == null) {
                return Optional.empty();
            }

            BigInteger rewards = delegationsAndRewardAccountsResult.getRewards() != null ?
                    delegationsAndRewardAccountsResult.getRewards().getOrDefault(address, BigInteger.ZERO) : BigInteger.ZERO;

            return Optional.of(new StakeAccountRewardInfo(stakeAddress, rewards));
        } finally {
            localClientProvider.ifPresent(localClientProviderManager::close);
        }
    }

}
