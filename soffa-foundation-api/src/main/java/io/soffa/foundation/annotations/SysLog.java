package io.soffa.foundation.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SysLog {

    String value() default "";
    boolean async() default true;

}
