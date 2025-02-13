package org.cardanofoundation.rosetta.yaciindexer.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.yaciindexer.domain.model.StakeAccountRewardInfo;
import org.cardanofoundation.rosetta.yaciindexer.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Optional;

import static com.bloxbean.cardano.yaci.store.common.util.Bech32Prefixes.STAKE_ADDR_PREFIX;

@RestController("rosetta.AccountController")
@RequestMapping("${apiPrefix}/rosetta/account")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rosetta Account API", description = "APIs for Rosetta's yaci-indexer account related operations.")
public class AccountController {

    private final AccountService accountService;

    @Value("${apiPrefix}/rosetta/account")
    private String path;

    @PostConstruct
    public void init() {
        log.info("Rosetta AccountController initialized, configured path: {}", path);
    }

    @GetMapping("/by-stake-address/{stakeAddress}")
    @Operation(description = "Obtain information about a specific stake account." +
            "It gets stake account balance from aggregated stake account balance if aggregation is enabled")
    public StakeAccountRewardInfo getStakeAccountDetails(@PathVariable("stakeAddress") @NonNull String stakeAddress) {
        if (!stakeAddress.startsWith(STAKE_ADDR_PREFIX)) {
            throw new IllegalArgumentException("Invalid stake address"); // TODO introduce problem from zalando?
        }

        Optional<StakeAccountRewardInfo> stakeAccountInfo = accountService.getAccountInfo(stakeAddress);

        return stakeAccountInfo.orElseGet(() -> new StakeAccountRewardInfo(stakeAddress, BigInteger.ZERO));
    }

}
