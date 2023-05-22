package org.cardanofoundation.rosetta.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TxIndexValidator implements ConstraintValidator<TxIndex, Short> {

  @Override
  public boolean isValid(Short aShort, ConstraintValidatorContext constraintValidatorContext) {
    return aShort >= 0;
  }
}
