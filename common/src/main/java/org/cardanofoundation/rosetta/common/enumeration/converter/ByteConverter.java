package org.cardanofoundation.rosetta.common.enumeration.converter;


import com.bloxbean.cardano.client.util.HexUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ByteConverter implements AttributeConverter<String, byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(String data) {
    return HexUtil.decodeHexString(data);
  }

  @Override
  public String convertToEntityAttribute(byte[] bytes) {
    return HexUtil.encodeHexString(bytes);
  }
}
