package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;
import org.openapitools.client.model.SubAccountIdentifier;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:35
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountIdentifier {
    @JsonProperty("address")
    private String address;

    @JsonProperty("sub_account")
    private SubAccountIdentifier subAccount;

    @JsonProperty("metadata")
    private Map<String, String> metadata;
}
