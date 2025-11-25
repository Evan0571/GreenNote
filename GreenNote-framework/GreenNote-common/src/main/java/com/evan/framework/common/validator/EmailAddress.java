package com.evan.framework.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailAddressValidator.class)
public @interface EmailAddress {
    String message() default "邮箱格式不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
