package io.soffa.foundation.core.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface BindAction {

    Class<?> value() default Object.class;

    String name() default "";

}
