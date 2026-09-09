package org.cardanofoundation.rosetta.api.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.util.Constants;

import lombok.NonNull;

import org.openapitools.client.model.*;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRegistryServiceImpl implements TokenRegistryService {

    private final TokenQueryServiceImpl tokenQueryService;

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> getTokenMetadataBatch(@NotNull Set<AssetFingerprint> assetFingerprints) {
        if (assetFingerprints.isEmpty()) {
            return Map.of();
        }

        return tokenQueryService.queryMetadataBatch(assetFingerprints);
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromBlockTx(@NonNull BlockTx blockTx) {
        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();

        Optional.ofNullable(blockTx.getInputs()).ifPresent(inputs ->
            inputs.forEach(input ->
                Optional.ofNullable(input.getAmounts()).ifPresent(amounts ->
                    allAssetFingerprints.addAll(extractAssetsFromAmounts(amounts)))));

        Optional.ofNullable(blockTx.getOutputs()).ifPresent(outputs ->
            outputs.forEach(output ->
                Optional.ofNullable(output.getAmounts()).ifPresent(amounts ->
                    allAssetFingerprints.addAll(extractAssetsFromAmounts(amounts)))));

        return allAssetFingerprints;
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromAmounts(@NonNull List<Amt> amounts) {
        return amounts.stream()
            .filter(amount -> amount.getPolicyId() != null)
            .filter(amount -> !LOVELACE.equals(amount.getUnit()))
            .map(amount -> AssetFingerprint.of(amount.getPolicyId(), amount.getSymbolHex()))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromBlockTransactions(@NotNull List<BlockTransaction> transactions) {
        if (transactions.isEmpty()) {
            return Set.of();
        }

        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();
        for (BlockTransaction blockTx : transactions) {
            Transaction tx = blockTx.getTransaction();
            allAssetFingerprints.addAll(extractAssetsFromOperations(tx.getOperations()));
        }

        return allAssetFingerprints;
    }

    @Override
    public Set<AssetFingerprint> extractAssetsFromOperations(@NotNull List<Operation> operations) {
        if (operations.isEmpty()) {
            return Set.of();
        }

        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();

        for (Operation operation : operations) {
            OperationMetadata metadata = operation.getMetadata();
            if (metadata != null) {
                List<TokenBundleItem> tokenBundle = metadata.getTokenBundle();
                if (tokenBundle != null) {
                    for (TokenBundleItem bundleItem : tokenBundle) {
                        String policyId = bundleItem.getPolicyId();
                        List<Amount> tokens = bundleItem.getTokens();
                        if (tokens != null) {
                            for (Amount tokenAmount : tokens) {
                                CurrencyResponse currency = tokenAmount.getCurrency();
                                if (currency != null) {
                                    String symbol = currency.getSymbol();
                                    if (!ADA.equals(symbol) && !LOVELACE.equals(symbol)) {
                                        allAssetFingerprints.add(AssetFingerprint.of(policyId, currency.getSymbol()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Amount amount = operation.getAmount();
            if (amount != null) {
                CurrencyResponse currency = amount.getCurrency();
                if (currency != null) {
                    String symbol = currency.getSymbol();
                    if (!LOVELACE.equals(symbol) && !ADA.equals(symbol)) {
                        CurrencyMetadataResponse currencyMetadata = currency.getMetadata();
                        if (currencyMetadata != null && currencyMetadata.getPolicyId() != null) {
                            allAssetFingerprints.add(AssetFingerprint.of(currencyMetadata.getPolicyId(), symbol));
                        }
                    }
                }
            }
        }

        return allAssetFingerprints;
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTx(@NotNull BlockTx blockTx) {
        Set<AssetFingerprint> assetFingerprints = extractAssetsFromBlockTx(blockTx);
        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }
        return getTokenMetadataBatch(assetFingerprints);
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTransactions(@NotNull List<BlockTransaction> transactions) {
        if (transactions.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<AssetFingerprint> assetFingerprints = extractAssetsFromBlockTransactions(transactions);
        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }
        return getTokenMetadataBatch(assetFingerprints);
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForBlockTxList(@NotNull List<BlockTx> blockTxList) {
        if (blockTxList == null || blockTxList.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<AssetFingerprint> allAssetFingerprints = new HashSet<>();
        for (BlockTx tx : blockTxList) {
            allAssetFingerprints.addAll(extractAssetsFromBlockTx(tx));
        }

        if (!allAssetFingerprints.isEmpty()) {
            return getTokenMetadataBatch(allAssetFingerprints);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForAddressBalances(@NotNull List<AddressBalance> balances) {
        Set<AssetFingerprint> assetFingerprints = balances.stream()
            .filter(b -> !LOVELACE.equals(b.unit()))
            .filter(b -> b.unit().length() >= Constants.POLICY_ID_LENGTH)
            .map(b -> AssetFingerprint.of(b.getPolicyId(), b.getSymbol()))
            .collect(Collectors.toSet());

        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }
        return getTokenMetadataBatch(assetFingerprints);
    }

    @Override
    public Map<AssetFingerprint, TokenRegistryCurrencyData> fetchMetadataForUtxos(@NotNull List<Utxo> utxos) {
        Set<AssetFingerprint> assetFingerprints = new HashSet<>();
        for (Utxo utxo : utxos) {
            for (Amt amount : utxo.getAmounts()) {
                if (amount.getPolicyId() != null && !LOVELACE.equals(amount.getUnit())) {
                    assetFingerprints.add(AssetFingerprint.of(amount.getPolicyId(), amount.getSymbolHex()));
                }
            }
        }

        if (assetFingerprints.isEmpty()) {
            return Collections.emptyMap();
        }

        return getTokenMetadataBatch(assetFingerprints);
    }

}
