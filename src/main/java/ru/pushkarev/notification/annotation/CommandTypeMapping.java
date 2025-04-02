package ru.pushkarev.notification.annotation;

import ru.pushkarev.notification.enums.CommandType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandTypeMapping {
    CommandType value();
}

