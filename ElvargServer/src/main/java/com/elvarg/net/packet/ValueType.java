package com.elvarg.net.packet;

/**
 * The enumerated type whose values represent the possible custom RuneScape
 * value types. Type {@code A} is to add 128 to the value, type {@code C} is to
 * invert the value, and type {@code S} is to subtract the value from 128. Of
 * course, {@code STANDARD} is just the normal data value.
 *
 * @author blakeman8192
 */
public enum ValueType {
    STANDARD,
    A,
    C,
    S
}