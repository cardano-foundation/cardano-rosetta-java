package org.cardanofoundation.rosetta.api.error.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.cardanofoundation.rosetta.api.error.service.BlockParsingErrorReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/error-review")
@RequiredArgsConstructor
public class BlockErrorReviewController {

    private final BlockParsingErrorReviewService blockParsingErrorReviewService;

    @PostMapping("/all")
    public ResponseEntity<List<BlockParsingErrorReviewDTO>> getAllErrorReviews(@RequestBody @Valid BlockParsingErrorsReadRequest blockParsingErrorsReadRequest) {
        return ResponseEntity.ok(blockParsingErrorReviewService.findTop1000(blockParsingErrorsReadRequest.getStatus()));
    }

    @PostMapping("/upsert")
    public ResponseEntity<List<BlockParsingErrorReviewDTO>> upsert(@RequestBody @Valid BlockParsingErrorReviewRequest blockParsingErrorReviewRequest) {
        int blockNumber = blockParsingErrorReviewRequest.getBlockNumber();

        List<BlockParsingErrorReviewDTO> top1000ByBlockNumber = blockParsingErrorReviewService.findTop1000ByBlockNumber(blockNumber);

        List<BlockParsingErrorReviewDTO> blockParsingErrorReviewDTOS = top1000ByBlockNumber.stream()
                .map(blockParsingErrorReviewDTO -> {
                    return blockParsingErrorReviewService.upset(
                                    blockParsingErrorReviewDTO.id(),
                                    blockParsingErrorReviewRequest.getStatus(),
                                    blockParsingErrorReviewRequest.getComment(),
                                    blockParsingErrorReviewRequest.getCheckedBy())
                            .map(errorReviewEntity -> new BlockParsingErrorReviewDTO(
                                    errorReviewEntity.getId(),
                                    blockParsingErrorReviewDTO.block(),
                                    blockParsingErrorReviewDTO.errorCode(),
                                    blockParsingErrorReviewDTO.reason(),
                                    blockParsingErrorReviewDTO.details(),
                                    errorReviewEntity.getStatus(),
                                    errorReviewEntity.getComment(),
                                    errorReviewEntity.getCheckedBy(),
                                    blockParsingErrorReviewDTO.note(),
                                    errorReviewEntity.getLastUpdated()));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return ResponseEntity.ok(blockParsingErrorReviewDTOS);
    }

}
