package org.cardanofoundation.rosetta.crawler.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.crawler.common.constants.Constants;
import org.cardanofoundation.rosetta.crawler.event.AppEvent;
import org.cardanofoundation.rosetta.crawler.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.crawler.model.Network;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.crawler.service.NetworkService;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@Slf4j
@Getter
public class NetworkValidationFilter extends OncePerRequestFilter {
  private List<String> excludedUrls;

  @Autowired
  private NetworkService networkService;

  @Autowired
  @Qualifier("handlerExceptionResolver")
  private HandlerExceptionResolver resolver;


//  @Override
//  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
//      FilterChain filterChain) throws IOException, ServletException {
//    HttpServletRequest request = (HttpServletRequest) servletRequest;
//    String body = request.getReader().lines().collect(Collectors.joining());
//    ObjectMapper mapper = new ObjectMapper();
//    NetworkRequest networkRequest  = mapper.readValue(body, NetworkRequest.class);
//    NetworkIdentifier identifier = networkRequest.getNetworkIdentifier();
//    log.debug("[networkValidation] About to validate requests network identifier parameter " + identifier);
//
//    String blockchain = identifier.getBlockchain();
//    String network = identifier.getNetwork();
//
//    if(!blockchain.equals(Constants.CARDANO)){
//      log.error("[networkValidation] Blockchain parameter is not cardano: " + blockchain);
//      throw ExceptionFactory.invalidBlockChainError();
//    }
//
//    boolean networkExists = networkService.getSupportedNetwork().getNetworkId().equals(network);
//    if(!networkExists){
//      log.error("[networkValidation] Network parameter is not supported: " + network);
//      throw ExceptionFactory.networkNotFoundError();
//    }
//    log.debug("[networkValidation] Network parameters are within expected");
//    filterChain.doFilter(servletRequest,servletResponse);
//  }

//  public Network getSupportedNetwork() {
//    if(AppEvent.networkId.equals("mainnet")){
//      return Network.builder().networkId(AppEvent.networkId).build();
//    } else if (AppEvent.networkMagic.equals(BigInteger.valueOf(Constants.PREPROD_NETWORK_MAGIC))) {
//      return Network.builder().networkId("preprod").build();
//    } else if (AppEvent.networkMagic.equals(BigInteger.valueOf(Constants.PREVIEW_NETWORK_MAGIC))) {
//      return Network.builder().networkId("preprod").build();
//    }
//    return null;
//  }

//  @Override
//  public void init(FilterConfig filterConfig) throws ServletException {
//    String excludePattern = filterConfig.getInitParameter("excludedUrls");
//    excludedUrls = Arrays.asList(excludePattern.split(","));
//  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
      String body = request.getReader().lines().collect(Collectors.joining());
      ObjectMapper mapper = new ObjectMapper();
      NetworkRequest networkRequest = mapper.readValue(body, NetworkRequest.class);
      NetworkIdentifier identifier = networkRequest.getNetworkIdentifier();
      log.debug("[networkValidation] About to validate requests network identifier parameter "
          + identifier);

      String blockchain = identifier.getBlockchain();
      String network = identifier.getNetwork();

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
      filterChain.doFilter(request, response);
    }
}
