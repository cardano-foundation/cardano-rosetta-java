package org.cardanofoundation.rosetta.common.ledgersync.byron.signature;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import lombok.Builder;
import lombok.Getter;
import org.cardanofoundation.rosetta.common.util.HexUtil;

import java.util.List;

@Builder
@Getter
public class Signature implements BlockSignature {

  private String blockSignature;

  @Override
  public String getType() {
    return ByronSigType.SIGNATURE;
  }

  public static org.cardanofoundation.rosetta.common.ledgersync.byron.signature.Signature deserialize(DataItem dataItem){
    List<DataItem> dataItems = ((Array) dataItem).getDataItems();

    String signature = HexUtil.encodeHexString((
        (ByteString)dataItems.get(0)).getBytes()
    );

    return org.cardanofoundation.rosetta.common.ledgersync.byron.signature.Signature.builder()
        .blockSignature(signature)
        .build();
  }
}
