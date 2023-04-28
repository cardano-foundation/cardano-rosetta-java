package org.cardanofoundation.rosetta.common.enumeration.converter;

import org.cardanofoundation.rosetta.common.enumeration.TokenType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class TokenTypeConverter implements AttributeConverter<TokenType,Integer> {

  @Override
  public Integer convertToDatabaseColumn(TokenType tokenType) {
    return tokenType.getValue();
  }

  @Override
  public TokenType convertToEntityAttribute(Integer type) {
    return TokenType.fromValue(type);
  }
}
