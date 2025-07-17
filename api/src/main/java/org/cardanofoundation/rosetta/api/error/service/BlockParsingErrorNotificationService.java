package org.cardanofoundation.rosetta.api.error.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.error.model.domain.BlockParsingErrorReviewDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.cardanofoundation.rosetta.api.error.model.domain.ReviewStatus.UNREVIEWED;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockParsingErrorNotificationService {

    private final BlockParsingErrorReviewService blockParsingErrorReviewService;

    @PostConstruct
    public void init() {
        checkBlockParsingErrors();
        log.info("BlockErrorNotificationService initialized.");
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void notifyErrors() {
        checkBlockParsingErrors();
    }

    private void checkBlockParsingErrors() {
        List<BlockParsingErrorReviewDTO> unreviewed = blockParsingErrorReviewService.findTop1000(UNREVIEWED);

        if (!unreviewed.isEmpty()) {
            log.error("You have unreviewed block parsing errors found" +
                    " in the database table: 'error'," +
                    " please check if you are affected via endpoint: /call (method: get_parse_error_blocks)." +
                    " Refer to this project's rosetta's documentation for the exact specification of the endpoint.");
        }
    }

}
