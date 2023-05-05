package org.cardanofoundation.rosetta.common.enumeration.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.cardanofoundation.rosetta.common.enumeration.SyncStateType;

@Converter(autoApply = true)
public class SyncStateTypeConverter implements AttributeConverter<SyncStateType, String> {

  @Override
  public String convertToDatabaseColumn(SyncStateType attribute) {
    return attribute.getValue();
  }

  @Override
  public SyncStateType convertToEntityAttribute(String dbData) {
    return SyncStateType.fromValue(dbData);
  }
}
