package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.EventsBlocksRequest;
import org.cardanofoundation.rosetta.api.model.rest.EventsBlocksResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class EventsApiDelegateImplementation implements EventsApiDelegate {
    @Override
    public ResponseEntity<EventsBlocksResponse> eventsBlocks(EventsBlocksRequest eventsBlocksRequest) {
        return null;
    }
}
