package org.cardanofoundation.rosetta.client;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YaciHttpGatewayImplTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private YaciHttpGatewayImpl yaciHttpGateway;

    private final String yaciBaseUrl = "http://localhost:8080";

    private final int httpRequestTimeoutSeconds = 10;
    private final String stakeAddress = "stake1u9p...";

    @BeforeEach
    void setUp() {
        yaciHttpGateway.httpRequestTimeoutSeconds = this.httpRequestTimeoutSeconds;
        yaciHttpGateway.yaciBaseUrl = this.yaciBaseUrl;
    }

    @Test
    void getStakeAccountRewards_Success() throws Exception {
        String jsonResponse = "{\"stakeAddress\": \"stake1u9p...\", \"balance\": 1000}";
        StakeAccountInfo expectedResponse = new StakeAccountInfo();

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(objectMapper.readValue(jsonResponse, StakeAccountInfo.class)).thenReturn(expectedResponse);

        StakeAccountInfo actualResponse = yaciHttpGateway.getStakeAccountRewards(stakeAddress);

        assertEquals(expectedResponse, actualResponse);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        assertEquals(yaciBaseUrl + "/rosetta/account/by-stake-address/" + stakeAddress, requestCaptor.getValue().uri().toString());
    }

    @Test
    void getStakeAccountRewards_BadRequest_ThrowsApiException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(400);

        ApiException exception = assertThrows(ApiException.class, () -> yaciHttpGateway.getStakeAccountRewards(stakeAddress));
        assertEquals(RosettaConstants.RosettaErrorType.GATEWAY_ERROR.toRosettaError(false), exception.getError());
    }

    @Test
    void getStakeAccountRewards_ServerError_ThrowsApiException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(500);

        ApiException exception = assertThrows(ApiException.class, () -> yaciHttpGateway.getStakeAccountRewards(stakeAddress));
        assertEquals(RosettaConstants.RosettaErrorType.GATEWAY_ERROR.toRosettaError(true), exception.getError());
    }

    @Test
    void getStakeAccountRewards_IOException_ThrowsApiException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("Network error"));

        ApiException exception = assertThrows(ApiException.class, () -> yaciHttpGateway.getStakeAccountRewards(stakeAddress));
        assertEquals(RosettaConstants.RosettaErrorType.GATEWAY_ERROR.toRosettaError(false), exception.getError());
    }

    @Test
    void getStakeAccountRewards_InterruptedException_ThrowsApiException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new InterruptedException("Request interrupted"));

        ApiException exception = assertThrows(ApiException.class, () -> yaciHttpGateway.getStakeAccountRewards(stakeAddress));
        assertEquals(RosettaConstants.RosettaErrorType.GATEWAY_ERROR.toRosettaError(false), exception.getError());
    }

    @Test
    void getStakeAccountRewards_UnexpectedStatusCode_ThrowsApiException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(403);

        ApiException exception = assertThrows(ApiException.class, () -> yaciHttpGateway.getStakeAccountRewards(stakeAddress));
        assertEquals(RosettaConstants.RosettaErrorType.GATEWAY_ERROR.toRosettaError(false), exception.getError());
    }

}
