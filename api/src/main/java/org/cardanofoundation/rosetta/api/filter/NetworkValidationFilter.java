package org.cardanofoundation.rosetta.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.filter.wrapper.RequestWrapper;
import org.cardanofoundation.rosetta.api.service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@Slf4j
@Getter
public class NetworkValidationFilter extends OncePerRequestFilter {
  private List<String> excludedUrls = List.of("/network/list");

  @Autowired
  private NetworkService networkService;

  @Autowired
  @Qualifier("handlerExceptionResolver")
  private HandlerExceptionResolver resolver;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
      RequestWrapper wrapper = new RequestWrapper(request);
      byte[] body = StreamUtils.copyToByteArray(wrapper.getInputStream());
      ObjectMapper mapper = new ObjectMapper();
      Map<String , Object> networkRequest = mapper.readValue(body, Map.class);
      Map<String, String> identifier = (LinkedHashMap<String, String>) networkRequest.get("network_identifier");
      log.debug("[networkValidation] About to validate requests network identifier parameter "
          + identifier);

      String blockchain = identifier.get("blockchain");
      String network = identifier.get("network");

      if (!blockchain.equals(Constants.CARDANO)) {
        log.error("[networkValidation] Blockchain parameter is not cardano: " + blockchain);
        throw ExceptionFactory.invalidBlockChainError();
      }

      boolean networkExists = networkService.getSupportedNetwork().getNetworkId().equals(network);
      if (!networkExists) {
        log.error("[networkValidation] Network parameter is not supported: " + network);
        throw ExceptionFactory.networkNotFoundError();
      }
      log.debug("[networkValidation] Network parameters are within expected");
      filterChain.doFilter(wrapper, response);
    }
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    for(String url : excludedUrls ){
      if (url.equals(path)){
        return true;
      }
    }
    return false;
  }
}
