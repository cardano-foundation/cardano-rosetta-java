package org.cardanofoundation.rosetta;

import java.util.List;
import jakarta.servlet.DispatcherType;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.openapitools.jackson.nullable.JsonNullableModule;

@SpringBootApplication
@EntityScan({
    "org.cardanofoundation.rosetta.api.account.model.entity",
    "org.cardanofoundation.rosetta.api.block.model.entity",
    "org.cardanofoundation.rosetta.api.construction.model.entity",
    "org.cardanofoundation.rosetta.api.network.model.entity",
    "org.cardanofoundation.rosetta.api.common.model.entity"})
@OpenAPIDefinition(info = @Info(title = "APIs", version = "1.0", description = "Rosetta APIs v1.0"))
@EnableScheduling
@EnableTransactionManagement
public class RosettaApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(RosettaApiApplication.class, args);
  }

  @Bean
  public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
    final ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
    final FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>(
        filter);
    registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC,
        DispatcherType.ERROR);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registration.setUrlPatterns(List.of("/**"));

    return registration;
  }

  @Bean
  @Profile("!test-integration")
  public JsonNullableModule jsonNullableModule() {
    return new JsonNullableModule();
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public CommonsRequestLoggingFilter logFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filter.setMaxPayloadLength(10000);
    filter.setIncludeHeaders(false);
    filter.setAfterMessagePrefix("REQUEST DATA: \n");

    return filter;
  }

}
