package org.cardanofoundation.rosetta.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Hash32TypeValidator implements ConstraintValidator<Hash32Type, String> {

  @Override
  public boolean isValid(String bytes, ConstraintValidatorContext constraintValidatorContext) {
    return bytes.length() == 64;
  }
}
