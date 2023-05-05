package org.cardanofoundation.rosetta.common.enumeration.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.cardanofoundation.rosetta.common.enumeration.ScriptType;

@Converter(autoApply = true)
public class ScriptTypeConverter implements AttributeConverter<ScriptType, String> {

  @Override
  public String convertToDatabaseColumn(ScriptType attribute) {
    return attribute.getValue();
  }

  @Override
  public ScriptType convertToEntityAttribute(String dbData) {
    return ScriptType.fromValue(dbData);
  }
}
