package org.cardanofoundation.rosetta.api.error.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.error.controller.requests.AllErrorRequest;
import org.cardanofoundation.rosetta.api.error.model.domain.ErrorDTO;
import org.cardanofoundation.rosetta.api.error.model.domain.entity.ErrorReview;
import org.cardanofoundation.rosetta.api.error.service.ErrorReviewService;
import org.cardanofoundation.rosetta.api.error.service.ErrorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/error-review")
@RequiredArgsConstructor
public class ErrorReviewController {

    public static final String ANONYMOUS_USER = "anonymous";

    private final ErrorService errorService;
    private final ErrorReviewService errorReviewService;
    private final Clock clock;

    @PostMapping("/all")
    public ResponseEntity<List<ErrorReview>> getAllErrorReviews(@RequestBody @Valid AllErrorRequest allErrorRequest) {
        List<ErrorDTO> mostRecentErrors = errorService.findMostRecentErrors();
        Map<Integer, ErrorDTO> errorMap = mostRecentErrors.stream()
                .collect(Collectors.toMap(ErrorDTO::getId, Function.identity()));

        List<Integer> ids = mostRecentErrors.stream()
                .map(ErrorDTO::getId)
                .toList();

        List<ErrorReview> errorReviews = errorReviewService.findByIds(ids)
                .stream()
                .sorted(comparing(errorReview -> {
                    ErrorDTO errorEntity = errorMap.get(errorReview.getId());

                    return errorEntity.getLastUpdated().toInstant(UTC);
                }))
                .toList();

        if (allErrorRequest.getStatus() != null) {
            List<ErrorReview> filteredErrors = errorReviews.stream()
                    .filter(errorReview -> errorReview.getStatus() == allErrorRequest.getStatus())
                    .toList();

            return ResponseEntity.ok(filteredErrors);
        }

        return ResponseEntity.ok(errorReviews);
    }

    @PostMapping("/upsert")
    public ResponseEntity<?> upsert(@RequestBody @Valid ErrorReviewRequest errorReviewRequest) {
        Integer errorId = errorReviewRequest.getId();

        Optional<ErrorDTO> errorM = errorService.findById(errorId);

        if (errorM.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ErrorReview errorReview = errorReviewService.findById(errorId)
                .orElseGet(() -> ErrorReview.builder()
                        .id(errorId)
                        .build());

        errorReview.setStatus(errorReviewRequest.getStatus());
        if (errorReviewRequest.getCheckedBy() == null) {
            errorReview.setCheckedBy(ANONYMOUS_USER);
        } else {
            errorReview.setCheckedBy(errorReviewRequest.getCheckedBy());
        }

        errorReview.setComment(errorReview.getComment());
        errorReview.setLastUpdated(LocalDateTime.now(clock));

        errorReviewService.upsert(errorReview);

        return ResponseEntity.ok().build();
    }

}
