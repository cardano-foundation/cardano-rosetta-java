//package org.cardanofoundation.rosetta.crawler.filter;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import org.cardanofoundation.rosetta.crawler.common.constants.Constants;
//import org.cardanofoundation.rosetta.crawler.exception.ExceptionFactory;
//import org.cardanofoundation.rosetta.crawler.model.rest.NetworkIdentifier;
//import org.cardanofoundation.rosetta.crawler.model.rest.NetworkRequest;
//import org.cardanofoundation.rosetta.crawler.service.NetworkService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.servlet.HandlerExceptionResolver;
//
//@Component
//@Slf4j
//@Getter
//public class NetworkValidationFilter extends OncePerRequestFilter {
//  private List<String> excludedUrls = List.of("/network/list");
//
//  @Autowired
//  private NetworkService networkService;
//
//  @Autowired
//  @Qualifier("handlerExceptionResolver")
//  private HandlerExceptionResolver resolver;
//
//  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//      FilterChain filterChain) throws ServletException, IOException {
//      String body = request.getReader().lines().collect(Collectors.joining());
//      ObjectMapper mapper = new ObjectMapper();
//      Map<String , Object> networkRequest = mapper.readValue(body, HashMap.class);
//      LinkedHashMap identifier = (LinkedHashMap) networkRequest.get("network_identifier");
//      log.debug("[networkValidation] About to validate requests network identifier parameter "
//          + identifier);
//
//      String blockchain = (String)identifier.get("blockchain");
//      String network = (String)identifier.get("network");
//
//      if (!blockchain.equals(Constants.CARDANO)) {
//        log.error("[networkValidation] Blockchain parameter is not cardano: " + blockchain);
//        throw ExceptionFactory.invalidBlockChainError();
//      }
//
//      boolean networkExists = networkService.getSupportedNetwork().getNetworkId().equals(network);
//      if (!networkExists) {
//        log.error("[networkValidation] Network parameter is not supported: " + network);
//        throw ExceptionFactory.networkNotFoundError();
//      }
//      log.debug("[networkValidation] Network parameters are within expected");
//      filterChain.doFilter(request, response);
//    }
//  @Override
//  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//    String path = request.getRequestURI();
//    for(String url : excludedUrls ){
//      if (url.equals(path)){
//        return true;
//      }
//    }
//    return false;
//  }
//}
