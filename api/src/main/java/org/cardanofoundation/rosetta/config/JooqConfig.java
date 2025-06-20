package org.cardanofoundation.rosetta.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        SQLDialect dialect = resolveDialectFromJdbcUrl(jdbcUrl);

        return DSL.using(dataSource, dialect);
    }

    private SQLDialect resolveDialectFromJdbcUrl(String url) {
        if (url.startsWith("jdbc:h2:")) {
            return SQLDialect.H2;
        }
        if (url.startsWith("jdbc:postgresql:")) {
            return SQLDialect.POSTGRES;
        }

        throw new IllegalArgumentException("Unsupported JDBC URL: " + url);
    }

}
