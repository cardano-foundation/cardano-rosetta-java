package org.cardanofoundation.rosetta.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Asset32TypeValidator implements ConstraintValidator<Asset32Type, byte[]> {

  @Override
  public boolean isValid(byte[] data, ConstraintValidatorContext constraintValidatorContext) {
    return data.length == 32;
  }
}
