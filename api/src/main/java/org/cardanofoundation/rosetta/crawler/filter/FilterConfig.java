package org.cardanofoundation.rosetta.crawler.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
  @Autowired
  private NetworkValidationFilter filter;
  @Bean
  public FilterRegistrationBean<NetworkValidationFilter> networkFilter(){
    FilterRegistrationBean<NetworkValidationFilter> validationFilter = new FilterRegistrationBean<>();
    validationFilter.setFilter(filter);
    validationFilter.setOrder(0);
    validationFilter.addUrlPatterns("/*");
    return validationFilter;
  }
}
