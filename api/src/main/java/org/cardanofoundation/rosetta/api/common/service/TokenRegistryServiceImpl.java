package org.cardanofoundation.rosetta.api.common.service;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenProperty;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.LogoType;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenRegistryServiceImpl implements TokenRegistryService {

    private final TokenRegistryHttpGateway tokenRegistryHttpGateway;

    @Override
    public Map<Asset, CurrencyMetadataResponse> getTokenMetadataBatch(Set<Asset> assets) {
        if (assets.isEmpty()) {
            return Map.of();
        }
        
        // Convert assets to subjects for the gateway call
        Set<String> subjects = assets.stream()
                .map(Asset::toSubject)
                .collect(Collectors.toSet());
        
        // Make the batch call to the gateway
        Map<String, Optional<TokenSubject>> tokenSubjectMap = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

        // Convert back to Asset -> CurrencyMetadataResponse mapping
        Map<Asset, CurrencyMetadataResponse> result = new HashMap<>();
        for (Asset asset : assets) {
            String subject = asset.toSubject();
            Optional<TokenSubject> tokenSubject = tokenSubjectMap.get(subject);
            
            if (tokenSubject != null && tokenSubject.isPresent()) {
                result.put(asset, extractTokenMetadata(asset.getPolicyId(), tokenSubject.get()));
            } else {
                // Always return fallback metadata with at least policyId
                result.put(asset, createFallbackMetadata(asset.getPolicyId()));
            }
        }
        
        return result;
    }

    private CurrencyMetadataResponse extractTokenMetadata(String policyId, TokenSubject tokenSubject) {
        CurrencyMetadataResponse.CurrencyMetadataResponseBuilder builder = CurrencyMetadataResponse.builder()
                .policyId(policyId);
        
        TokenMetadata tokenMeta = tokenSubject.getMetadata();
        
        // Mandatory fields from registry API related to token data
        builder.subject(tokenSubject.getSubject());
        builder.name(tokenMeta.getName().getValue());
        builder.description(tokenMeta.getDescription().getValue());
        
        // Optional fields
        Optional.ofNullable(tokenMeta.getTicker()).ifPresent(ticker -> builder.ticker(ticker.getValue()));
        Optional.ofNullable(tokenMeta.getUrl()).ifPresent(url -> builder.url(url.getValue()));
        Optional.ofNullable(tokenMeta.getLogo()).ifPresent(logo -> builder.logo(convertToLogoType(logo)));
        Optional.ofNullable(tokenMeta.getVersion()).ifPresent(version -> builder.version(BigDecimal.valueOf(version.getValue())));

        // Set decimals, defaulting to 0 if not found
        int decimals = Optional.ofNullable(tokenMeta.getDecimals())
                .map(TokenPropertyNumber::getValue)
                .map(Long::intValue)
                .orElse(0);
        builder.decimals(decimals);
        
        return builder.build();
    }

    @Nullable
    private LogoType convertToLogoType(TokenProperty logoProperty) {
        if (logoProperty == null) {
            return null;
        }
        String source = logoProperty.getSource();
        String value = logoProperty.getValue();

        return LogoType.builder()
                .format(getFormatEnum(source))
                .value(value)
                .build();
    }

    @Nullable
    private static LogoType.FormatEnum getFormatEnum(@NotNull String source) {
        return switch (source.toLowerCase()) {
            case "cip_26" -> LogoType.FormatEnum.BASE64;
            case "cip_68" -> LogoType.FormatEnum.URL;
            default -> null;
        };
    }

    private CurrencyMetadataResponse createFallbackMetadata(String policyId) {
        return CurrencyMetadataResponse.builder()
                .policyId(policyId)
                .build();
    }

}
