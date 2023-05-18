//package org.cardanofoundation.rosetta.crawler.filter;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FilterConfig {
//  @Autowired
//  private NetworkValidationFilter filter;
//  @Autowired
//  private FilterChainExceptionHandler handler;
//  @Bean
//  public FilterRegistrationBean<NetworkValidationFilter> networkFilter(){
//    FilterRegistrationBean<NetworkValidationFilter> validationFilter = new FilterRegistrationBean<>();
//    validationFilter.setFilter(filter);
//    validationFilter.setOrder(1);
//    validationFilter.addUrlPatterns("/*");
//    return validationFilter;
//  }
//  @Bean
//  public FilterRegistrationBean<FilterChainExceptionHandler> filterExceptionHandler(){
//    FilterRegistrationBean<FilterChainExceptionHandler> exceptionHandler = new FilterRegistrationBean<>();
//    exceptionHandler.setFilter(handler);
//    exceptionHandler.setOrder(0);
//    return exceptionHandler;
//  }
//}
