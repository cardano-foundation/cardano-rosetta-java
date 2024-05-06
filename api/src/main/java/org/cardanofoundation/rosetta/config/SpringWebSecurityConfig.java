package org.cardanofoundation.rosetta.config;

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
  @Bean
  public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
    http
        // we don't need CSRF protection yet, since Rosetta API is used by non-browser clients only
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(matcherRegistry -> matcherRegistry
            .requestMatchers("/**").permitAll())
        .headers(headers -> headers
            .contentSecurityPolicy(policy -> policy.policyDirectives("default-src 'self'"))
            .referrerPolicy(
                policy -> policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
            .permissionsPolicy(policy -> policy.policy("geolocation=(self)")));


    return http.build();
  }

}
