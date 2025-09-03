package org.cardanofoundation.rosetta.yaciindexer.resource;

import com.bloxbean.cardano.yaci.core.protocol.peersharing.messages.PeerAddress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.yaciindexer.domain.model.PeerAddressDto;
import org.cardanofoundation.rosetta.yaciindexer.service.PeerDiscoveryManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("rosetta.PeersDiscoveryController")
@RequestMapping("${apiPrefix}/rosetta/peers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rosetta Peers Discovery API", description = "APIs for discovering and retrieving Cardano network peers.")
public class PeersDiscoveryResource {

    private final PeerDiscoveryManager peerDiscoveryManager;

    @Value("${apiPrefix}/rosetta/peers")
    private String path;

    @PostConstruct
    public void init() {
        log.info("Rosetta PeersDiscoveryController initialized, configured path: {}", path);
    }

    @GetMapping
    @Operation(
            summary = "Get discovered Cardano network peers",
            description = "Returns a cached list of discovered Cardano network peers. " +
                    "The peer list is automatically refreshed every minute in the background. " +
                    "This endpoint provides fast access to the most recently discovered peers without " +
                    "blocking on network operations."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the list of discovered peers",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PeerAddressDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public List<PeerAddressDto> getDiscoveredPeers() {
        log.debug("Retrieving cached peers from PeerDiscoveryManager");
        
        List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();
        
        log.info("Returning {} cached peers", cachedPeers.size());
        
        return cachedPeers.stream()
                .map(this::toPeerAddressDto)
                .toList();
    }

    private PeerAddressDto toPeerAddressDto(PeerAddress peerAddress) {
        return PeerAddressDto.builder()
                .type(peerAddress.getType().name())
                .address(peerAddress.getAddress())
                .port(peerAddress.getPort())
                .build();
    }

}