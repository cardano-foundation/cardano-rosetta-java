package org.cardanofoundation.rosetta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.client.model.domain.TokenRegistryBatchResponse;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deserialization of {@code POST /api/v2/subjects/query} responses in the shape served by
 * cf-token-metadata-registry 2.0.0: new {@code type}, {@code validation_errors}, {@code metadata.valid}
 * and {@code metadata.empty} fields, missing properties as explicit {@code null}, tokens without
 * {@code name}/{@code description}, and an empty {@code metadata} block for a token without metadata.
 */
class TokenRegistryV2ResponseDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should deserialize a valid 2.0.0 subject, ignoring the new fields")
    void shouldDeserializeValidSubject() throws Exception {
        String json = """
            {
              "subjects": [
                {
                  "subject": "c6e65ba7878b2f8ea0ad39287d3e2fd256dc5c4160fc19bdf4c4d87e7447454e53",
                  "type": "NATIVE",
                  "metadata": {
                    "name": { "value": "Genius Yield Test Token", "source": "CIP_26" },
                    "description": { "value": "Test token for Genius Yield", "source": "CIP_26" },
                    "ticker": { "value": "tGENS", "source": "CIP_26" },
                    "decimals": { "value": 6, "source": "CIP_26" },
                    "logo": null,
                    "url": { "value": "https://preprod.genius-x.co", "source": "CIP_26" },
                    "version": null,
                    "valid": true,
                    "empty": false
                  },
                  "standards": null,
                  "validation_errors": []
                }
              ],
              "queryPriority": ["CIP_68", "CIP_26"]
            }
            """;

        TokenRegistryBatchResponse response = objectMapper.readValue(json, TokenRegistryBatchResponse.class);

        assertThat(response.getSubjects()).singleElement().satisfies(subject -> {
            assertThat(subject.getMetadata()).isNotNull();
            assertThat(subject.getMetadata().getName()).isNotNull();
            assertThat(subject.getMetadata().getName().getValue()).isEqualTo("Genius Yield Test Token");
            assertThat(subject.getMetadata().getDecimals().getValue()).isEqualTo(6L);
            assertThat(subject.getMetadata().getLogo()).isNull();
            assertThat(subject.getMetadata().getVersion()).isNull();
        });
    }

    @Test
    @DisplayName("Should deserialize a 2.0.0 subject with validation errors and no name or description")
    void shouldDeserializeSubjectWithoutNameAndDescription() throws Exception {
        String json = """
            {
              "subjects": [
                {
                  "subject": "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d354657374546f6b656e",
                  "type": "NATIVE",
                  "metadata": {
                    "name": null,
                    "description": null,
                    "ticker": { "value": "TST", "source": "CIP_26" },
                    "decimals": null,
                    "logo": null,
                    "url": null,
                    "version": null,
                    "valid": false,
                    "empty": false
                  },
                  "standards": null,
                  "validation_errors": [
                    {
                      "cip": "CIP_26",
                      "sub_standard": null,
                      "field_name": "name",
                      "field_category": "MANDATORY",
                      "code": "missing",
                      "error": "name is required"
                    }
                  ]
                }
              ],
              "queryPriority": ["CIP_68", "CIP_26"]
            }
            """;

        TokenRegistryBatchResponse response = objectMapper.readValue(json, TokenRegistryBatchResponse.class);

        assertThat(response.getSubjects()).singleElement().satisfies(subject -> {
            assertThat(subject.getMetadata()).isNotNull();
            assertThat(subject.getMetadata().getName()).isNull();
            assertThat(subject.getMetadata().getDescription()).isNull();
            assertThat(subject.getMetadata().getTicker().getValue()).isEqualTo("TST");
        });
    }

    @Test
    @DisplayName("Should deserialize a 2.0.0 CIP-113 programmable subject with an empty metadata block")
    void shouldDeserializeProgrammableSubjectWithEmptyMetadata() throws Exception {
        String json = """
            {
              "subjects": [
                {
                  "subject": "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d354657374546f6b656e",
                  "type": "PROGRAMMABLE",
                  "metadata": {
                    "name": null,
                    "description": null,
                    "ticker": null,
                    "decimals": null,
                    "logo": null,
                    "url": null,
                    "version": null,
                    "valid": false,
                    "empty": true
                  },
                  "standards": null,
                  "validation_errors": [
                    {
                      "cip": "CIP_113",
                      "sub_standard": null,
                      "field_name": "metadata",
                      "field_category": "MANDATORY",
                      "code": "companion_metadata",
                      "error": "no CIP-26 or CIP-68 metadata"
                    }
                  ]
                }
              ],
              "queryPriority": ["CIP_68", "CIP_26"]
            }
            """;

        TokenRegistryBatchResponse response = objectMapper.readValue(json, TokenRegistryBatchResponse.class);

        assertThat(response.getSubjects()).singleElement()
            .extracting(TokenSubject::getMetadata)
            .isNotNull()
            .satisfies(metadata -> {
                assertThat(metadata.getName()).isNull();
                assertThat(metadata.getDescription()).isNull();
                assertThat(metadata.getDecimals()).isNull();
            });
    }

}
