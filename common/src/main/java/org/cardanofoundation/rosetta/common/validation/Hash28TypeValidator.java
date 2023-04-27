package org.cardanofoundation.rosetta.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Hash28TypeValidator implements ConstraintValidator<Hash28Type, String> {

  @Override
  public boolean isValid(String bytes, ConstraintValidatorContext constraintValidatorContext) {
    return bytes.length() == 56;
  }
}
