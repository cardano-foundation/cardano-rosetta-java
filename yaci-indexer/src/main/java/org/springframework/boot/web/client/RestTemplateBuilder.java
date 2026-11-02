package org.springframework.boot.web.client;

import java.time.Duration;

import org.springframework.web.client.RestTemplate;

/**
 * Binary compatibility for Spring Boot 3 {@code RestTemplateBuilder}.
 * Boot 4 moved the type to {@code org.springframework.boot.restclient.RestTemplateBuilder}
 * and renamed {@code setConnectTimeout}/{@code setReadTimeout}.
 */
public class RestTemplateBuilder {

  private final org.springframework.boot.restclient.RestTemplateBuilder delegate;

  public RestTemplateBuilder(org.springframework.boot.restclient.RestTemplateBuilder delegate) {
    this.delegate = delegate;
  }

  public RestTemplateBuilder setConnectTimeout(Duration connectTimeout) {
    return new RestTemplateBuilder(delegate.connectTimeout(connectTimeout));
  }

  public RestTemplateBuilder setReadTimeout(Duration readTimeout) {
    return new RestTemplateBuilder(delegate.readTimeout(readTimeout));
  }

  public RestTemplate build() {
    return delegate.build();
  }
}
