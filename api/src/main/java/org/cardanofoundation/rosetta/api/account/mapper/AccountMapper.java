package org.cardanofoundation.rosetta.api.account.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsResponse;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class, uses = {AccountMapperUtil.class})
public interface AccountMapper {

  /**
   * Maps a list of AddressBalanceDTOs to a Rosetta compatible AccountBalanceResponse.
   *
   * @param block    The block from where the balances are calculated into the past
   * @param balances The list of filtered balances up to {@code block} number. Each unit should
   *                 occur only one time with the latest balance. Native assets should be present
   *                 only as a lovelace unit.
   * @return The Rosetta compatible AccountBalanceResponse
   */
  @Mapping(target = "blockIdentifier.hash", source = "block.hash")
  @Mapping(target = "blockIdentifier.index", source = "block.number")
  @Mapping(target = "balances", source = "balances", qualifiedByName = "mapAddressBalancesToAmounts")
  AccountBalanceResponse mapToAccountBalanceResponse(BlockIdentifierExtended block,
      List<AddressBalance> balances,
      @Context Map<Asset, TokenRegistryCurrencyData> metadataMap);

  @Mapping(target = "blockIdentifier.hash", source = "block.hash")
  @Mapping(target = "blockIdentifier.index", source = "block.number")
  @Mapping(target = "coins", source = "utxos", qualifiedByName = "mapUtxosToCoins")
  AccountCoinsResponse mapToAccountCoinsResponse(BlockIdentifierExtended block,
      List<Utxo> utxos,
      @Context Map<Asset, TokenRegistryCurrencyData> metadataMap);
}
