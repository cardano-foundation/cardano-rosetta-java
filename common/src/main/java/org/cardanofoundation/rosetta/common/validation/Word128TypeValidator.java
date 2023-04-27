package org.cardanofoundation.rosetta.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigInteger;

public class Word128TypeValidator implements ConstraintValidator<Word128Type, BigInteger> {

  @Override
  public boolean isValid(BigInteger number, ConstraintValidatorContext constraintValidatorContext) {
    return number.compareTo(BigInteger.valueOf(0L)) >= 0
        || number.compareTo(new BigInteger("340282366920938463463374607431768211455")) < 0;
  }
}
