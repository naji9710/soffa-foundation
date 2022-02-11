package io.soffa.foundation.core.annotations;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.lang.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JsonModel{}
