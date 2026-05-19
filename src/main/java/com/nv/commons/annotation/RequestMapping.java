package com.nv.commons.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.nv.commons.constants.HttpMethodType;
import com.nv.module.backendapi.constants.APIAuthType;

@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface RequestMapping {

	String path();

	HttpMethodType method() default HttpMethodType.UNSPECIFIED;

	APIAuthType authType() default APIAuthType.MUST_BE_LOGIN;
}
