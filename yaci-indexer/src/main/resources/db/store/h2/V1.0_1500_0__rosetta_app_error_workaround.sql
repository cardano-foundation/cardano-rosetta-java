-- this should not be here, this is a workaround for the fact that the
-- yaci-store does not create the error table for whatever reason
-- it is defined in yaci store core module

CREATE TABLE IF NOT EXISTS error (
    id               INT NOT NULL AUTO_INCREMENT,
    block            BIGINT,
    error_code       VARCHAR(64) NOT NULL,
    reason           TEXT NOT NULL,
    details          TEXT,
    update_datetime  TIMESTAMP,
    PRIMARY KEY (id)
);
