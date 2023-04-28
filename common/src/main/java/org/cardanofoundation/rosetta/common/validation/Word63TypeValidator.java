package org.cardanofoundation.rosetta.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Word63TypeValidator implements ConstraintValidator<Word63Type, Long> {

  @Override
  public boolean isValid(Long aLong, ConstraintValidatorContext constraintValidatorContext) {
    return aLong >= 0;
  }
}
