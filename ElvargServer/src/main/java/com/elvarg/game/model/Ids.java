package com.elvarg.game.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ids {

    /**
     * This holds the array of IDs associated with a given entity
     * @return
     */
    int[] value();
}