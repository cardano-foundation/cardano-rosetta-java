package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.CallRequest;
import org.cardanofoundation.rosetta.api.model.rest.CallResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CallApiDelegateImplementation implements CallApiDelegate {
    @Override
    public ResponseEntity<CallResponse> call(CallRequest callRequest) {
        return CallApiDelegate.super.call(callRequest);
    }
}
