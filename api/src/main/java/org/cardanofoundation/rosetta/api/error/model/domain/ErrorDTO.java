package org.cardanofoundation.rosetta.api.error.model.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {

    private Integer id;
    private Long block;
    private String errorCode;
    private String reason;
    private String details;
    private LocalDateTime lastUpdated;

}
