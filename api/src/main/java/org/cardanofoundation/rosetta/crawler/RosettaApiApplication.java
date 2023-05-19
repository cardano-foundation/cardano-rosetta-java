package org.cardanofoundation.rosetta.crawler;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.servlet.DispatcherType;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.filter.ForwardedHeaderFilter;


@SpringBootApplication
@EnableJpaRepositories({"org.cardanofoundation.rosetta", "org.cardanofoundation.rosetta.common"})
@EntityScan({"org.cardanofoundation.rosetta.common", "org.cardanofoundation.rosetta"})
@ComponentScan({"org.cardanofoundation.rosetta.common", "org.cardanofoundation.rosetta"})
@OpenAPIDefinition(info = @Info(title = "APIs", version = "1.0", description = "Rosetta APIs v1.0"))
public class RosettaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RosettaApiApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        final ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        final FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setUrlPatterns(List.of("/**"));
        return registration;
    }
}
