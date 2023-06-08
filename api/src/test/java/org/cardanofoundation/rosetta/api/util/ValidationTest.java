package org.cardanofoundation.rosetta.api.util;

import static org.cardanofoundation.rosetta.api.util.Validations.isVoteDataValid;
import static org.cardanofoundation.rosetta.api.util.Validations.isVoteSignatureValid;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ValidationTest {

  @Test
  void test_isVoteDataValid_true_when_vote_data_metadata_format_is_valid() {
    Map<String, Object> validDataLabel = new HashMap<>();
    validDataLabel.put("1", "0x8bcec4282239b2cc1a7d8bb294c154c849fc200c7ebd27ef45e610d849bc302a");
    validDataLabel.put("2", "0x56f29f391a3bb5ff90637b2d2d0a32590214871284b0577e4671b0c1a83f79ba");
    validDataLabel.put("3",
        "0x01663f13971437b6e2f09771c06534c4ffd95950ac94390f34e091b5ba8cc49dce93335c74cb3aaf8e0f7eacb8813ae4a107383ee7649985e6");
    validDataLabel.put("4", 26912766);

    assertTrue(isVoteDataValid(validDataLabel));
  }

  @Test
  void test_isVoteDataValid_true_when_vote_data_metadata_format_is_valid_besides_hex_string_does_not_tart_with_0x() {
    Map<String, Object> validDataLabel = new HashMap<>();
    validDataLabel.put("1", "8bcec4282239b2cc1a7d8bb294c154c849fc200c7ebd27ef45e610d849bc302a");
    validDataLabel.put("2", "56f29f391a3bb5ff90637b2d2d0a32590214871284b0577e4671b0c1a83f79ba");
    validDataLabel.put("3",
        "01663f13971437b6e2f09771c06534c4ffd95950ac94390f34e091b5ba8cc49dce93335c74cb3aaf8e0f7eacb8813ae4a107383ee7649985e6");
    validDataLabel.put("4", 26912766);

    assertTrue(isVoteDataValid(validDataLabel));
  }

  @Test
  void test_isVoteDataValid_falsy_when_there_are_missing_fields() {
    Map<String, Object> missingFieldsDataLabel = new HashMap<>();
    missingFieldsDataLabel.put("1",
        "0x8bcec4282239b2cc1a7d8bb294c154c849fc200c7ebd27ef45e610d849bc302a");
    missingFieldsDataLabel.put("2",
        "0x56f29f391a3bb5ff90637b2d2d0a32590214871284b0577e4671b0c1a83f79ba");
    missingFieldsDataLabel.put("4", 26912766);

    assertFalse(isVoteDataValid(missingFieldsDataLabel));
  }

  @Test
  void test_isVoteDataValid_false_when_expected_hex_string_has_invalid_format() {
    Map<String, Object> invalidHexStringDataLabel = new HashMap<>();
    invalidHexStringDataLabel.put("1",
        "0x8bcec4282239b2cc1a7d8bb294c154c849fc200c7ebd27ef45e610d849bc302a");
    invalidHexStringDataLabel.put("2", "thisIsNotAHexString");
    invalidHexStringDataLabel.put("3",
        "0x01663f13971437b6e2f09771c06534c4ffd95950ac94390f34e091b5ba8cc49dce93335c74cb3aaf8e0f7eacb8813ae4a107383ee7649985e6");
    invalidHexStringDataLabel.put("4", 26912766);

    assertFalse(isVoteDataValid(invalidHexStringDataLabel));
  }

  @Test
  void test_isVoteDataValid_false_when_expected_number_has_invalid_format() {
    Map<String, Object> invalidNumberDataLabel = new HashMap<>();
    invalidNumberDataLabel.put("1",
        "0x8bcec4282239b2cc1a7d8bb294c154c849fc200c7ebd27ef45e610d849bc302a");
    invalidNumberDataLabel.put("2", "thisIsNotAHexString");
    invalidNumberDataLabel.put("3",
        "0x01663f13971437b6e2f09771c06534c4ffd95950ac94390f34e091b5ba8cc49dce93335c74cb3aaf8e0f7eacb8813ae4a107383ee7649985e6");
    invalidNumberDataLabel.put("4", "NaN");

    assertFalse(isVoteDataValid(invalidNumberDataLabel));
  }

  @Test
  void test_isVoteSignatureValid_true_when_vote_signature_metadata_format_is_valid() {
    Map<String, Object> validSignatureLabel = new HashMap<>();
    validSignatureLabel.put("1",
        "0xf75f7a54a79352f9d0e2c4de4e8ded8ae9304fa0f3b021754f8d149c90c7b01e1c6bbfdd623c294d82f5e5cbbfc0bd6fd1c674780db4025446e2eafc87f61b0a");

    assertTrue(isVoteSignatureValid(validSignatureLabel));
  }

  @Test
  void test_isVoteSignatureValid_true_when_vote_signature_label_format_is_valid_besides_hex_string_does_not_start_with_0x() {
    Map<String, Object> validSignatureLabel = new HashMap<>();
    validSignatureLabel.put("1",
        "f75f7a54a79352f9d0e2c4de4e8ded8ae9304fa0f3b021754f8d149c90c7b01e1c6bbfdd623c294d82f5e5cbbfc0bd6fd1c674780db4025446e2eafc87f61b0a");

    assertTrue(isVoteSignatureValid(validSignatureLabel));
  }

  @Test
  void test_isVoteSignatureValid_falsy_when_there_is_a_missing_field() {
    Map<String, Object> invalidSignatureLabel = new HashMap<>();
    invalidSignatureLabel.put("2",
        "0xf75f7a54a79352f9d0e2c4de4e8ded8ae9304fa0f3b021754f8d149c90c7b01e1c6bbfdd623c294d82f5e5cbbfc0bd6fd1c674780db4025446e2eafc87f61b0a");

    assertFalse(isVoteSignatureValid(invalidSignatureLabel));
  }

  @Test
  public void test_isVoteSignatureValid_false_when_expected_hex_string_has_invalid_format() {
    Map<String, Object> invalidSignatureLabel = new HashMap<>();
    invalidSignatureLabel.put("1", "thisIsNotAHexString");

    assertFalse(isVoteSignatureValid(invalidSignatureLabel));
  }
}
