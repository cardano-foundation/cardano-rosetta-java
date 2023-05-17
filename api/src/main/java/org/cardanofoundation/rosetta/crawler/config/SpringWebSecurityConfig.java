package org.cardanofoundation.rosetta.crawler.config;

import org.cardanofoundation.rosetta.crawler.filter.FilterChainExceptionHandler;
import org.cardanofoundation.rosetta.crawler.filter.NetworkValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SpringWebSecurityConfig {
  @Autowired
  private FilterChainExceptionHandler filterChainExceptionHandler;
  @Autowired
  private NetworkValidationFilter networkValidationFilter;
  @Bean
  public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(matcherRegistry -> matcherRegistry
            .requestMatchers("/**").permitAll())
        .headers(headers -> headers
            .contentSecurityPolicy((policy) -> policy.policyDirectives("default-src 'self'"))
            .referrerPolicy(
                policy -> policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
            .permissionsPolicy((policy) -> policy.policy("geolocation=(self)")))
        .addFilterBefore(filterChainExceptionHandler , NetworkValidationFilter.class);


    return http.build();
  }

}
