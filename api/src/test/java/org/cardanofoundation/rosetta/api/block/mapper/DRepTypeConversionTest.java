package org.cardanofoundation.rosetta.api.block.mapper;

import org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapperImpl;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DRepType conversion logic in TransactionMapper.
 * Tests the enum conversions between Yaci and cardano-client-lib DRepType enums.
 *
 * This test validates the critical business logic of converting between:
 * - com.bloxbean.cardano.yaci.core.model.governance.DrepType (entity layer)
 * - com.bloxbean.cardano.client.transaction.spec.governance.DRepType (domain layer)
 */
@ExtendWith(MockitoExtension.class)
class DRepTypeConversionTest {

  @Mock
  private ProtocolParamService protocolParamService;
  private TransactionMapper transactionMapper;

  @BeforeEach
  void setUp() {
    // Create real instances with dependencies
    DataMapper dataMapper = new DataMapper(new TokenRegistryMapperImpl());
    TransactionMapperUtils transactionMapperUtils = new TransactionMapperUtils(
            protocolParamService,
            dataMapper
    );

    transactionMapper = new TransactionMapperImpl(transactionMapperUtils);
  }

  @Nested
  class YaciToClientConversionTests {

    @Test
    void shouldConvertAddrKeyHash() {
      // given
      com.bloxbean.cardano.yaci.core.model.governance.DrepType yaciType =
          com.bloxbean.cardano.yaci.core.model.governance.DrepType.ADDR_KEYHASH;

      // when
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType result =
          transactionMapper.convertYaciDrepType(yaciType);

      // then
      assertThat(result).isEqualTo(com.bloxbean.cardano.client.transaction.spec.governance.DRepType.ADDR_KEYHASH);
    }

    @Test
    void shouldConvertScriptHash() {
      // given
      com.bloxbean.cardano.yaci.core.model.governance.DrepType yaciType =
          com.bloxbean.cardano.yaci.core.model.governance.DrepType.SCRIPTHASH;

      // when
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType result =
          transactionMapper.convertYaciDrepType(yaciType);

      // then
      assertThat(result).isEqualTo(com.bloxbean.cardano.client.transaction.spec.governance.DRepType.SCRIPTHASH);
    }

    @Test
    void shouldConvertAbstain() {
      // given
      com.bloxbean.cardano.yaci.core.model.governance.DrepType yaciType =
          com.bloxbean.cardano.yaci.core.model.governance.DrepType.ABSTAIN;

      // when
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType result =
          transactionMapper.convertYaciDrepType(yaciType);

      // then
      assertThat(result).isEqualTo(com.bloxbean.cardano.client.transaction.spec.governance.DRepType.ABSTAIN);
    }

    @Test
    void shouldConvertNoConfidence() {
      // given
      com.bloxbean.cardano.yaci.core.model.governance.DrepType yaciType =
          com.bloxbean.cardano.yaci.core.model.governance.DrepType.NO_CONFIDENCE;

      // when
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType result =
          transactionMapper.convertYaciDrepType(yaciType);

      // then
      assertThat(result).isEqualTo(com.bloxbean.cardano.client.transaction.spec.governance.DRepType.NO_CONFIDENCE);
    }

    @Test
    void shouldHandleNull() {
      // when
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType result =
          transactionMapper.convertYaciDrepType(null);

      // then
      assertThat(result).isNull();
    }

    @Test
    void shouldConvertAllYaciTypesSuccessfully() {
      // Test all Yaci enum values convert without error
      for (com.bloxbean.cardano.yaci.core.model.governance.DrepType yaciType :
          com.bloxbean.cardano.yaci.core.model.governance.DrepType.values()) {

        // when
        com.bloxbean.cardano.client.transaction.spec.governance.DRepType result =
            transactionMapper.convertYaciDrepType(yaciType);

        // then
        assertThat(result)
            .as("Conversion of %s should not be null", yaciType.name())
            .isNotNull();
        assertThat(result.name())
            .as("Enum names should match for %s", yaciType.name())
            .isEqualTo(yaciType.name());
      }
    }
  }

  @Nested
  class ClientToYaciConversionTests {

    @Test
    void shouldConvertAddrKeyHash() {
      // given
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType clientType =
          com.bloxbean.cardano.client.transaction.spec.governance.DRepType.ADDR_KEYHASH;

      // when
      com.bloxbean.cardano.yaci.core.model.governance.DrepType result =
          transactionMapper.convertClientDrepType(clientType);

      // then
      assertThat(result).isEqualTo(com.bloxbean.cardano.yaci.core.model.governance.DrepType.ADDR_KEYHASH);
    }

    @Test
    void shouldConvertScriptHash() {
      // given
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType clientType =
          com.bloxbean.cardano.client.transaction.spec.governance.DRepType.SCRIPTHASH;

      // when
      com.bloxbean.cardano.yaci.core.model.governance.DrepType result =
          transactionMapper.convertClientDrepType(clientType);

      // then
      assertThat(result).isEqualTo(com.bloxbean.cardano.yaci.core.model.governance.DrepType.SCRIPTHASH);
    }

    @Test
    void shouldConvertAbstain() {
      // given
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType clientType =
          com.bloxbean.cardano.client.transaction.spec.governance.DRepType.ABSTAIN;

      // when
      com.bloxbean.cardano.yaci.core.model.governance.DrepType result =
          transactionMapper.convertClientDrepType(clientType);

      // then
      assertThat(result).isEqualTo(com.bloxbean.cardano.yaci.core.model.governance.DrepType.ABSTAIN);
    }

    @Test
    void shouldConvertNoConfidence() {
      // given
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType clientType =
          com.bloxbean.cardano.client.transaction.spec.governance.DRepType.NO_CONFIDENCE;

      // when
      com.bloxbean.cardano.yaci.core.model.governance.DrepType result =
          transactionMapper.convertClientDrepType(clientType);

      // then
      assertThat(result).isEqualTo(com.bloxbean.cardano.yaci.core.model.governance.DrepType.NO_CONFIDENCE);
    }

    @Test
    void shouldHandleNull() {
      // when
      com.bloxbean.cardano.yaci.core.model.governance.DrepType result =
          transactionMapper.convertClientDrepType(null);

      // then
      assertThat(result).isNull();
    }

    @Test
    void shouldConvertAllClientTypesSuccessfully() {
      // Test all Client enum values convert without error
      for (com.bloxbean.cardano.client.transaction.spec.governance.DRepType clientType :
          com.bloxbean.cardano.client.transaction.spec.governance.DRepType.values()) {

        // when
        com.bloxbean.cardano.yaci.core.model.governance.DrepType result =
            transactionMapper.convertClientDrepType(clientType);

        // then
        assertThat(result)
            .as("Conversion of %s should not be null", clientType.name())
            .isNotNull();
        assertThat(result.name())
            .as("Enum names should match for %s", clientType.name())
            .isEqualTo(clientType.name());
      }
    }
  }

  @Nested
  class BidirectionalConversionTests {

    @Test
    void shouldRoundTripYaciToClientAndBack() {
      // Test complete round-trip conversion for all values
      for (com.bloxbean.cardano.yaci.core.model.governance.DrepType original :
          com.bloxbean.cardano.yaci.core.model.governance.DrepType.values()) {

        // when - convert Yaci -> Client -> Yaci
        com.bloxbean.cardano.client.transaction.spec.governance.DRepType clientType =
            transactionMapper.convertYaciDrepType(original);
        com.bloxbean.cardano.yaci.core.model.governance.DrepType result =
            transactionMapper.convertClientDrepType(clientType);

        // then
        assertThat(result)
            .as("Round-trip conversion for %s", original.name())
            .isEqualTo(original);
      }
    }

    @Test
    void shouldRoundTripClientToYaciAndBack() {
      // Test complete round-trip conversion for all values
      for (com.bloxbean.cardano.client.transaction.spec.governance.DRepType original :
          com.bloxbean.cardano.client.transaction.spec.governance.DRepType.values()) {

        // when - convert Client -> Yaci -> Client
        com.bloxbean.cardano.yaci.core.model.governance.DrepType yaciType =
            transactionMapper.convertClientDrepType(original);
        com.bloxbean.cardano.client.transaction.spec.governance.DRepType result =
            transactionMapper.convertYaciDrepType(yaciType);

        // then
        assertThat(result)
            .as("Round-trip conversion for %s", original.name())
            .isEqualTo(original);
      }
    }

    @Test
    void shouldMaintainEnumNameConsistency() {
      // Verify that enum names are identical across both types
      com.bloxbean.cardano.yaci.core.model.governance.DrepType[] yaciValues =
          com.bloxbean.cardano.yaci.core.model.governance.DrepType.values();
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType[] clientValues =
          com.bloxbean.cardano.client.transaction.spec.governance.DRepType.values();

      assertThat(yaciValues).hasSameSizeAs(clientValues);

      for (int i = 0; i < yaciValues.length; i++) {
        assertThat(yaciValues[i].name())
            .as("Enum name at index %d", i)
            .isEqualTo(clientValues[i].name());
      }
    }
  }

}
