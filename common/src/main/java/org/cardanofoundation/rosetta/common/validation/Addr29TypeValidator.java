package org.cardanofoundation.rosetta.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Addr29TypeValidator implements ConstraintValidator<Addr29Type, String> {

  @Override
  public boolean isValid(String bytes, ConstraintValidatorContext constraintValidatorContext) {
    return bytes.length() == 58;
  }
}
