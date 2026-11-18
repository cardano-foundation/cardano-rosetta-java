package org.cardanofoundation.rosetta.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Base64UtilsTest {

    @Test
    void decodesValidBase64() {
        assertThat(Base64Utils.decode("iVBORw0KGgo=")).hasValueSatisfying(bytes ->
                assertThat(bytes).containsExactly(0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'));
    }

    @Test
    void decodesEmptyInputToNoBytes() {
        assertThat(Base64Utils.decode("")).hasValueSatisfying(bytes -> assertThat(bytes).isEmpty());
    }

    @Test
    void returnsEmptyForCharactersOutsideTheAlphabet() {
        assertThat(Base64Utils.decode("not a logo!!")).isEmpty();
    }

    @Test
    void returnsEmptyForMisplacedPadding() {
        assertThat(Base64Utils.decode("iVBO=w0K")).isEmpty();
    }

}
