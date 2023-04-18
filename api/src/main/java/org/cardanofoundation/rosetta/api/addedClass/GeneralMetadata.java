package org.cardanofoundation.rosetta.api.addedClass;

import com.bloxbean.cardano.client.metadata.Metadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GeneralMetadata {
    private long labelNumber;
    private Metadata metadata;
}
