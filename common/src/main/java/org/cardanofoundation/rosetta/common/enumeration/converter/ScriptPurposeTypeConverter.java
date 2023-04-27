package org.cardanofoundation.rosetta.common.enumeration.converter;


import org.cardanofoundation.rosetta.common.enumeration.ScriptPurposeType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ScriptPurposeTypeConverter implements AttributeConverter<ScriptPurposeType, String> {

  @Override
  public String convertToDatabaseColumn(ScriptPurposeType attribute) {
    return attribute.getValue();
  }

  @Override
  public ScriptPurposeType convertToEntityAttribute(String dbData) {
    return ScriptPurposeType.fromValue(dbData);
  }
}
