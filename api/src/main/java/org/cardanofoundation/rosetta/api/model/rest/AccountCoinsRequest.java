package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.Valid;
import org.cardanofoundation.rosetta.api.model.Currency;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalanceRequest {
  @JsonProperty("network_identifier")

  private NetworkIdentifier networkIdentifier;
  @JsonProperty("account_identifier")

  private AccountIdentifier accountIdentifier;
  @JsonProperty("block_identifier")
  private PartialBlockIdentifier blockIdentifier;

  @JsonProperty("include_mempool")
  private Boolean includeMempool;

  @JsonProperty("currencies")
  @Valid
  private List<Currency> currencies = null;
}
