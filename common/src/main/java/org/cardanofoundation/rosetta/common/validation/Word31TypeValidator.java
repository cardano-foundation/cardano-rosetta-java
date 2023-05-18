package org.cardanofoundation.rosetta.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Word31TypeValidator implements ConstraintValidator<Word31Type, Integer> {

  @Override
  public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
    return integer >= 0;
  }
}
