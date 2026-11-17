package org.cardanofoundation.rosetta.common.enumeration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.CurveType;
import org.openapitools.client.model.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The OpenAPI generator is configured with {@code useEnumCaseInsensitive=true}: enum values in
 * requests are accepted in any case, while responses keep the lowercase values from the spec.
 */
@DisplayName("Generated API enums")
class GeneratedEnumCaseInsensitivityTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @ParameterizedTest
  @ValueSource(strings = {"coin_spent", "COIN_SPENT", "Coin_Spent"})
  @DisplayName("accept coin_action in any case")
  void acceptsCoinActionInAnyCase(String value) throws Exception {
    CoinChange coinChange = objectMapper.readValue(
        "{\"coin_identifier\":{\"identifier\":\"abc:0\"},\"coin_action\":\"" + value + "\"}",
        CoinChange.class);

    assertThat(coinChange.getCoinAction()).isEqualTo(CoinAction.SPENT);
  }

  @ParameterizedTest
  @ValueSource(strings = {"edwards25519", "EDWARDS25519", "Edwards25519"})
  @DisplayName("accept curve_type in any case")
  void acceptsCurveTypeInAnyCase(String value) throws Exception {
    PublicKey publicKey = objectMapper.readValue(
        "{\"hex_bytes\":\"00\",\"curve_type\":\"" + value + "\"}", PublicKey.class);

    assertThat(publicKey.getCurveType()).isEqualTo(CurveType.EDWARDS25519);
  }

  @Test
  @DisplayName("serialise enum values in the spec's lowercase form")
  void serialisesLowercase() throws Exception {
    PublicKey publicKey = objectMapper.readValue(
        "{\"hex_bytes\":\"00\",\"curve_type\":\"EDWARDS25519\"}", PublicKey.class);

    assertThat(objectMapper.writeValueAsString(publicKey)).contains("\"curve_type\":\"edwards25519\"");
  }

  @Test
  @DisplayName("still reject unknown enum values")
  void rejectsUnknownValues() {
    assertThatThrownBy(() -> objectMapper.readValue(
        "{\"hex_bytes\":\"00\",\"curve_type\":\"ed448\"}", PublicKey.class))
        .isInstanceOf(ValueInstantiationException.class)
        .hasMessageContaining("Unexpected value 'ed448'");
  }
}
