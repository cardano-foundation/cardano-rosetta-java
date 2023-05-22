package org.cardanofoundation.rosetta.api.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import org.openapitools.client.model.Amount;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:36
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalanceResponse {
    BlockIdentifier blockIdentifier;
    List<Amount> balances;
    Object metadata;

}
