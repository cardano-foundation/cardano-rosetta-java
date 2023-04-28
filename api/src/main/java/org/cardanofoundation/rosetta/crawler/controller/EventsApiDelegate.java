package org.cardanofoundation.rosetta.crawler.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cardanofoundation.rosetta.crawler.model.rest.EventsBlocksRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.EventsBlocksResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:29
 */
@Tag(name = "v1-api", description = "The Cardano Events API")
public interface EventsApiDelegate {

  @Operation(
          operationId = "eventsBlocks",
          summary = "/events/blocks allows the caller to query a sequence of BlockEvents indicating which blocks were added and removed from storage to reach the current state.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/events/blocks",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<EventsBlocksResponse> eventsBlocks(EventsBlocksRequest eventsBlocksRequest);
}
