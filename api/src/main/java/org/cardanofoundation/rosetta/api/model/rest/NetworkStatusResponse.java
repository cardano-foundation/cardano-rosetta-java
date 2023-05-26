package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import org.cardanofoundation.rosetta.api.model.Peer;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:34
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class NetworkStatusResponse {
    @JsonProperty("current_block_identifier")
    private BlockIdentifier currentBlockIdentifier;
    @JsonProperty("current_block_timestamp")
    private Long currentBlockTimeStamp;
    @JsonProperty("genesis_block_identifier")
    private BlockIdentifier genesisBlockIdentifier;
    @JsonProperty("peers")
    private List<Peer> peers ;
}
