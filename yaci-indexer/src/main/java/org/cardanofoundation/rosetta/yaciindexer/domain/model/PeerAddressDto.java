package org.cardanofoundation.rosetta.yaciindexer.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Cardano network peer address information")
public class PeerAddressDto {
    
    @Schema(description = "Address type (IPv4 or IPv6)", example = "IPv4")
    private String type;
    
    @Schema(description = "IP address of the peer", example = "192.168.1.100")
    private String address;
    
    @Schema(description = "Port number of the peer", example = "30000")
    private Integer port;
}