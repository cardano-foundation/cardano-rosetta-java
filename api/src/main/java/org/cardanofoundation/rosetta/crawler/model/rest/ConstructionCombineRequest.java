package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.rosetta.crawler.model.Signature;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:13
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ConstructionCombineRequest {
    @JsonProperty("network_identifier")
    private NetworkIdentifier networkIdentifier;

    @JsonProperty("unsigned_transaction")
    private String unsignedTransaction;

    @JsonProperty("signatures")
    @Valid
    private List<Signature> signatures = new ArrayList<>();
}
