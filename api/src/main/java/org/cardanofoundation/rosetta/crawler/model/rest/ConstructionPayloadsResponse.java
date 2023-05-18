package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.rosetta.crawler.model.SigningPayload;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:14
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConstructionPayloadsResponse {
    @JsonProperty("unsigned_transaction")
    private String unsignedTransaction;

    @JsonProperty("payloads")
    @Valid
    private List<SigningPayload> payloads = new ArrayList<>();
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConstructionPayloadsResponse {\n");
        sb.append("    unsignedTransaction: ").append(toIndentedString(unsignedTransaction)).append("\n");
        sb.append("    payloads: ").append(toIndentedString(payloads)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
