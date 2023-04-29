package com.sid.gl.validators;

import com.sid.gl.dto.NewPasswordRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
        //
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        final NewPasswordRequest dto = (NewPasswordRequest) obj;
        return dto.getPassword().equals(dto.getConfirmPassword());
    }
}
