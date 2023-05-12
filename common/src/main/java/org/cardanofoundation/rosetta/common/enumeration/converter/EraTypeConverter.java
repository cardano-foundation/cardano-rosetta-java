package org.cardanofoundation.rosetta.common.enumeration.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.cardanofoundation.rosetta.common.enumeration.EraType;

@Converter(autoApply = true)
public class EraTypeConverter implements AttributeConverter<EraType, Integer> {

  @Override
  public Integer convertToDatabaseColumn(EraType attribute) {
    return attribute.getValue();
  }

  @Override
  public EraType convertToEntityAttribute(Integer dbData) {
    return EraType.valueOf(dbData);
  }
}
