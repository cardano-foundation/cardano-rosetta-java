package org.cardanofoundation.rosetta.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigInteger;

public class Int65TypeValidator implements ConstraintValidator<Int65Type, BigInteger> {

  @Override
  public boolean isValid(BigInteger number, ConstraintValidatorContext constraintValidatorContext) {
    return number.compareTo(new BigInteger("-18446744073709551615")) >= 0
        && number.compareTo(new BigInteger("18446744073709551615")) <= 0;
  }
}
