package org.cardanofoundation.rosetta.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigInteger;

public class Word64TypeValidator implements ConstraintValidator<Word64Type, BigInteger> {

  @Override
  public boolean isValid(BigInteger number, ConstraintValidatorContext constraintValidatorContext) {
    return number.compareTo(BigInteger.valueOf(0L)) >= 0
        && number.compareTo(new BigInteger("18446744073709551615")) <= 0;
  }
}
