package com.evan.framework.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailAddressValidator implements ConstraintValidator<EmailAddress, String> {

    @Override
    public void initialize(EmailAddress constraintAnnotation) {
    }

    @Override
    public boolean isValid(String emailAddress, ConstraintValidatorContext context) {
        if (emailAddress == null)
            return false;
        return emailAddress.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
