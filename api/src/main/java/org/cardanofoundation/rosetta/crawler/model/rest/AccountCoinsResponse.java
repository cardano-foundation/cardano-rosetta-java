package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.cardanofoundation.rosetta.crawler.model.Coin;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:36
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCoinsResponse {
    @JsonProperty("block_identifier")
    private BlockIdentifier blockIdentifier;

    @JsonProperty("coins")
    @Valid
    private List<Coin> coins = new ArrayList<>();

    @JsonProperty("metadata")
    private Object metadata;
}
