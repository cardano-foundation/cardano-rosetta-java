package org.cardanofoundation.rosetta.api.account.mapper;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

class AddressBalanceMapperImplTest {

    private AddressBalanceMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new AddressBalanceMapperImpl();
    }

    @Test
    void shouldConvertStakeAccountInfoToAddressBalanceSuccessfully() {
        // Arrange
        StakeAccountInfo stakeAccountInfo = StakeAccountInfo.builder()
                .stakeAddress("stake1uyp0rq3kwzg0lh6u6yw7a2xwkzf8hxxhkzjlmse4alv8c7gaq4qx7")
                .withdrawableAmount(BigInteger.valueOf(5_000_000L)) // lovelaces
                .build();

        Long number = 123L;

        // Act
        AddressBalance result = mapper.convertToAdaAddressBalance(stakeAccountInfo, number);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.address()).isEqualTo("stake1uyp0rq3kwzg0lh6u6yw7a2xwkzf8hxxhkzjlmse4alv8c7gaq4qx7");
        assertThat(result.unit()).isEqualTo(LOVELACE);
        assertThat(result.quantity()).isEqualTo(5_000_000L);
        assertThat(result.number()).isEqualTo(123L);
    }

    @Test
    void shouldHandleNullValuesGracefully() {
        // Arrange
        StakeAccountInfo stakeAccountInfo = new StakeAccountInfo();
        Long number = null;

        // Act
        AddressBalance result = mapper.convertToAdaAddressBalance(stakeAccountInfo, number);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.address()).isNull();
        assertThat(result.unit()).isEqualTo(LOVELACE);
        assertThat(result.quantity()).isNull();
        assertThat(result.number()).isNull();
    }

}
