package org.cardanofoundation.rosetta.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TxIndexValidator implements ConstraintValidator<TxIndex, Short> {

  @Override
  public boolean isValid(Short aShort, ConstraintValidatorContext constraintValidatorContext) {
    return aShort >= 0;
  }
}
