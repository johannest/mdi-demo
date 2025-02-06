package com.example.application.components.window;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WindowContent {

    String value();
    
    String title();

    String width() default "100%";
    
    String height() default "100%";
    
    String left() default "0px";

    String top() default "50px";

    boolean showInManager() default true;

    boolean multiWindow() default true;
}
