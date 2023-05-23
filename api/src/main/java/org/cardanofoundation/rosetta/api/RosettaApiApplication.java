package org.cardanofoundation.rosetta.api;

import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.servlet.DispatcherType;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;


@SpringBootApplication
@EntityScan({"org.cardanofoundation.rosetta.common"})
@OpenAPIDefinition(info = @Info(title = "APIs", version = "1.0", description = "Rosetta APIs v1.0"))
public class RosettaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RosettaApiApplication.class, args);
    }
    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
            .directory("./api")
            .ignoreIfMissing()
            .load();
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
