package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.List;
import org.openapitools.client.model.Currency;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCoinsRequest {
    @JsonProperty("network_identifier")

    private NetworkIdentifier networkIdentifier;
    @JsonProperty("account_identifier")

    private AccountIdentifier accountIdentifier;
    @JsonProperty("include_mempool")
    private Boolean includeMempool;

    @JsonProperty("currencies")
    @Valid
    private List<Currency> currencies = null;
}
