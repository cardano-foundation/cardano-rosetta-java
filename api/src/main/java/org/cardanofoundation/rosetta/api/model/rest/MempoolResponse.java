package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MempoolResponse {
  @JsonProperty("transaction_identifier")
  private List<TransactionIdentifier> transactionIdentifierList;
}
