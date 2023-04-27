package org.cardanofoundation.rosetta.common.enumeration.converter;

import org.cardanofoundation.rosetta.common.enumeration.RewardType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RewardTypeConverter implements AttributeConverter<RewardType, String> {

  @Override
  public String convertToDatabaseColumn(RewardType attribute) {
    return attribute.getValue();
  }

  @Override
  public RewardType convertToEntityAttribute(String dbData) {
    return RewardType.fromValue(dbData);
  }
}
