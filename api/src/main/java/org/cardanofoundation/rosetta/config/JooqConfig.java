package org.cardanofoundation.rosetta.config;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class JooqConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.jooq.sql-dialect", havingValue = "H2")
    public org.jooq.Configuration h2JooqConfiguration(DataSource dataSource) {
        log.info("Configuring jOOQ for H2 database with transaction-aware connection management");

        // Wrap the DataSource with TransactionAwareDataSourceProxy to ensure
        // JOOQ uses the same connection as Spring's transaction manager
        TransactionAwareDataSourceProxy transactionAwareDataSource = 
            new TransactionAwareDataSourceProxy(dataSource);

        Settings settings = new Settings()
                .withRenderSchema(false)
                .withRenderCatalog(false)
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
                .withRenderNameCase(RenderNameCase.UPPER);
        
        return new DefaultConfiguration()
                .set(SQLDialect.H2)
                .set(settings)
                .set(transactionAwareDataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.jooq.sql-dialect", havingValue = "H2")
    public DSLContext h2DSLContext(org.jooq.Configuration h2JooqConfiguration) {
        return new DefaultDSLContext(h2JooqConfiguration);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.jooq.sql-dialect", havingValue = "POSTGRES", matchIfMissing = true)
    public org.jooq.Configuration postgresJooqConfiguration(DataSource dataSource) {
        log.info("Configuring jOOQ for PostgreSQL database with transaction-aware connection management");
        
        // Wrap the DataSource with TransactionAwareDataSourceProxy to ensure
        // JOOQ uses the same connection as Spring's transaction manager
        // This is crucial for temporary table operations that must share the same connection
        TransactionAwareDataSourceProxy transactionAwareDataSource = 
            new TransactionAwareDataSourceProxy(dataSource);
        
        Settings settings = new Settings()
                .withRenderSchema(false)
                .withRenderCatalog(false)
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
                .withRenderNameCase(RenderNameCase.LOWER);
        
        return new DefaultConfiguration()
                .set(SQLDialect.POSTGRES)
                .set(settings)
                .set(transactionAwareDataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.jooq.sql-dialect", havingValue = "POSTGRES", matchIfMissing = true)
    public DSLContext postgresqlDSLContext(org.jooq.Configuration postgresJooqConfiguration) {
        return new DefaultDSLContext(postgresJooqConfiguration);
    }
}