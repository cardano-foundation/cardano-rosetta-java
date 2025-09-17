package org.cardanofoundation.rosetta.api.common.service.impl;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenProperty;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.LogoType;
import org.springframework.stereotype.Service;

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

    private LogoType convertToLogoType(TokenProperty logoProperty) {
        if (logoProperty == null || logoProperty.getValue() == null) {
            return null;
        }
        
        String source = logoProperty.getSource();
        String value = logoProperty.getValue();

        LogoType.FormatEnum format = getFormatEnum(source);

        return LogoType.builder()
                .format(format)
                .value(value)
                .build();
    }

    private static LogoType.FormatEnum getFormatEnum(String source) {
        // Determine format based on CIP source
        LogoType.FormatEnum format;
        if ("CIP_26".equalsIgnoreCase(source)) {
            // CIP_26 uses hex bytes - convert to base64
            format = LogoType.FormatEnum.BASE64;
        } else if ("CIP_68".equalsIgnoreCase(source)) {
            // CIP_68 uses URLs
            format = LogoType.FormatEnum.URL;
        } else {
            // Default to URL for unknown sources
            format = LogoType.FormatEnum.URL;
        }

        return format;
    }

    private CurrencyMetadataResponse createFallbackMetadata(String policyId) {
        return CurrencyMetadataResponse.builder()
                .policyId(policyId)
                .build();
    }

}
