package org.cardanofoundation.rosetta.api.account.service;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType.TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressHistoryServiceHibernateTest {

  private static final String ADDRESS = "addr_test1_address";

  @Mock
  private AddressUtxoRepository addressUtxoRepository;

  @InjectMocks
  private AddressHistoryServiceHibernate addressHistoryService;

  @Test
  void findCompleteTransactionHistoryByAddress_mergesAndDeduplicatesInputsAndOutputs() {
    when(addressUtxoRepository.findOutputTransactionsByAddress(ADDRESS)).thenReturn(List.of("tx1", "tx2"));
    when(addressUtxoRepository.findInputTransactionsByAddress(ADDRESS)).thenReturn(List.of("tx2", "tx3"));

    List<String> result = addressHistoryService.findCompleteTransactionHistoryByAddress(ADDRESS);

    assertThat(result).containsExactly("tx1", "tx2", "tx3");
  }

  @Test
  void findCompleteTransactionHistoryByAddress_throwsTimeoutError_whenDbQueriesExceedTimeout() {
    ReflectionTestUtils.setField(addressHistoryService, "addressHistoryApiTimeoutSecs", 1);
    when(addressUtxoRepository.findOutputTransactionsByAddress(ADDRESS)).thenAnswer(invocation -> {
      Thread.sleep(10_000); // interrupted when the scope times out
      return List.of();
    });

    ApiException exception = assertThrows(ApiException.class, () ->
        addressHistoryService.findCompleteTransactionHistoryByAddress(ADDRESS));

    assertThat(exception.getError().getCode()).isEqualTo(TIMEOUT.getCode());
    assertThat(exception.getError().isRetriable()).isTrue();
  }

}
