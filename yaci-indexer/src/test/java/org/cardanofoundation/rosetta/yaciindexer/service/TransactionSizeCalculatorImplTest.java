package org.cardanofoundation.rosetta.yaciindexer.service;

import org.junit.jupiter.api.Test;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.NegativeInteger;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;

import static co.nstant.in.cbor.model.SimpleValue.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

class TransactionSizeCalculatorImplTest {

  private static final String SIGNED_TX = "84a3008182582047f2a85b713bcf587bd46896da628fd80c268665d9b86050a6a8cf9030c5b8d3010182825839006971735734b6c29b6ab065142aaf89eb524d1ea6a293bb43b672a1bae8f8fdcc485472d5a3a6c2416bfc4bc0fa81dbcc9bc4317a4a68e1631a004c4b4082583900a8b3b814a65e5e43777f89b30c87eac4e3396a9b9c24cd08d66094998e5053d0eb80dda519e85bf8e08144bedb5485f8808502aa538447731a040c7d64021a0002a121a10081825820d498c3d54941e01cbcfb565371a844f1342edee82795ef5297de45be3acb19d85840dced88adf01fa809af2db1c76c5273394f1bca48c0c6709800b469231608713916d2292a644a503c275492ef68aa29ec8c5543034897ded231c461eb34d9180cf5f6";

  private final TransactionSizeCalculatorImpl calculator = new TransactionSizeCalculatorImpl();

  @Test
  void calculateSize_realTransaction_matchesCanonicalEncoder() {
    DataItem tx = CborSerializationUtil.deserialize(HexUtil.decodeHexString(SIGNED_TX))[0];
    Map signedTransaction = new Map();
    for (int i = 0; i < 4; i++) {
      signedTransaction.put(new UnsignedInteger(i), ((Array) tx).getDataItems().get(i));
    }

    assertThat(calculator.calculateSize(signedTransaction))
        .isEqualTo(CborSerializationUtil.serialize(signedTransaction).length);
  }

  @Test
  void calculateSize_unsortedMapsTagsAndNesting_matchesCanonicalEncoder() {
    Map metadata = new Map();
    metadata.put(new UnicodeString("zz"), new ByteString(new byte[]{1, 2, 3}));
    metadata.put(new UnsignedInteger(721), new NegativeInteger(-5));
    metadata.put(new UnicodeString("a"), TRUE);
    Array tagged = new Array();
    tagged.add(metadata);
    tagged.setTag(258);
    Array outer = new Array();
    outer.add(tagged);
    outer.add(new Array().add(new Map()));
    Map signedTransaction = new Map();
    signedTransaction.put(new UnsignedInteger(3), outer);
    signedTransaction.put(new UnsignedInteger(0), new UnsignedInteger(1));

    assertThat(calculator.calculateSize(signedTransaction))
        .isEqualTo(CborSerializationUtil.serialize(signedTransaction).length);
  }

  @Test
  void calculateSize_deeplyNestedMetadata_doesNotOverflowStack() {
    int depth = 20_000;
    DataItem nested = new UnsignedInteger(0);
    for (int i = 0; i < depth; i++) {
      Array wrapper = new Array();
      wrapper.add(nested);
      nested = wrapper;
    }
    Map tx = new Map();
    tx.put(new UnsignedInteger(3), nested);

    assertThat(calculator.calculateSize(tx)).isEqualTo(depth + 3);
  }
}
