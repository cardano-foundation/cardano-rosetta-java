package org.cardanofoundation.rosetta.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Hash28TypeValidator implements ConstraintValidator<Hash28Type, String> {

  @Override
  public boolean isValid(String bytes, ConstraintValidatorContext constraintValidatorContext) {
    return bytes.length() == 56;
  }
}
