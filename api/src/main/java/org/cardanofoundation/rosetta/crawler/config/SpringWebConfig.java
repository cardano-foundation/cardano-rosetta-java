package org.cardanofoundation.rosetta.crawler.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class SpringWebConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(final FormatterRegistry registry) {
    registry.addConverter(new Converter<String, Map<String, String>>() {
      @Override
      public Map<String, String> convert(@NonNull final String source) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
          return objectMapper.readValue(source, Map.class);
        } catch (JsonProcessingException e) {
          return null;
        }
      }
    });

    registry.addConverter(new Converter<String, LinkedHashMap<String, String>>() {
      @Override
      public LinkedHashMap<String, String> convert(@NonNull final String source) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
          return objectMapper.readValue(source, LinkedHashMap.class);
        } catch (JsonProcessingException e) {
          return null;
        }
      }
    });
  }
}
