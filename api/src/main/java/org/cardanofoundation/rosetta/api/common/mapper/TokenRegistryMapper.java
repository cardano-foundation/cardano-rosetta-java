package org.cardanofoundation.rosetta.api.common.mapper;

import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.LogoType;

/**
 * Mapper for converting domain TokenRegistryCurrencyData to view/serialization CurrencyMetadataResponse.
 * This mapper is responsible for the serialization layer, converting internal domain objects
 * to OpenAPI-generated response models.
 */
@Mapper(config = BaseMapper.class)
public interface TokenRegistryMapper {

    /**
     * Maps TokenRegistryCurrencyData to CurrencyMetadataResponse.
     * Note: decimals field is intentionally not mapped as it should not be in the response.
     *
     * @param data The domain object containing token registry data
     * @return The OpenAPI response object for serialization
     */
    @Mapping(target = "policyId", source = "policyId")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "ticker", source = "ticker")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "logo", source = "logo", qualifiedByName = "mapLogoData")
    CurrencyMetadataResponse toCurrencyMetadataResponse(TokenRegistryCurrencyData data);

    /**
     * Maps LogoData to LogoType.
     *
     * @param logoData The domain logo data
     * @return The OpenAPI LogoType
     */
    @org.mapstruct.Named("mapLogoData")
    default LogoType mapLogoData(TokenRegistryCurrencyData.LogoData logoData) {
        if (logoData == null) {
            return null;
        }

        LogoType.FormatEnum format = null;
        if (logoData.getFormat() != null) {
            format = switch (logoData.getFormat()) {
                case BASE64 -> LogoType.FormatEnum.BASE64;
                case URL -> LogoType.FormatEnum.URL;
            };
        }

        return LogoType.builder()
                .format(format)
                .value(logoData.getValue())
                .build();
    }
}