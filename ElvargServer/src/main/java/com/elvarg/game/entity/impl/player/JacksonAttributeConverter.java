package com.elvarg.game.entity.impl.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class JacksonAttributeConverter<T> implements AttributeConverter<T> {
    private static final Gson gson = new GsonBuilder().create();
    private final Class<T> clazz;

    public JacksonAttributeConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public AttributeValue transformFrom(T input) {
        return AttributeValue
                .builder()
                .s(gson.toJson(input))
                .build();
    }

    @Override
    public T transformTo(AttributeValue input) {
        return gson.fromJson(input.s(), this.clazz);
    }

    @Override
    public EnhancedType type() {
        return EnhancedType.of(this.clazz);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}