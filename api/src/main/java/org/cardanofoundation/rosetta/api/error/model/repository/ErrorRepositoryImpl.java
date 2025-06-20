package org.cardanofoundation.rosetta.api.error.model.repository;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.error.model.domain.ErrorDTO;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ErrorRepositoryImpl implements ErrorRepository {

    private final DSLContext dsl;

    private final Integer ERROR_RECORDS_LIMIT = 1000; // Limit for the number of records to fetch

    @Override
    public boolean hasErrors() {
        return dsl.fetchExists(
            DSL.selectOne().from("error")
        );
    }

    @Override
    public List<ErrorDTO> findMostRecentErrors() {
        Result<Record> result = dsl.select()
                .from("error")
                .limit(ERROR_RECORDS_LIMIT) // Limit to 1,000 records for performance
                .fetch();

        return result.stream()
                .map(ErrorRepositoryImpl::convertRecordToErrorEntity)
                .toList();
    }

    @Override
    public Optional<ErrorDTO> findById(Integer id) {
        Record record = dsl.select()
                .from("error")
                .where(DSL.field("id").eq(id))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        return Optional.of(convertRecordToErrorEntity(record));
    }

    private static ErrorDTO convertRecordToErrorEntity(Record record) {
        return ErrorDTO.builder()
                .id(record.get("id", Integer.class))
                .block(record.get("block", Long.class))
                .errorCode(record.get("error_code", String.class))
                .reason(record.get("reason", String.class))
                .details(record.get("details", String.class))
                .lastUpdated(record.get("update_datetime", LocalDateTime.class))
                .build();
    }

}
