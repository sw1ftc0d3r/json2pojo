package com.swiftcoder.json2pojo.models;

import java.util.Objects;

public class JsonField {
    private final String name;
    private final String type;
    private final boolean isArray;
    private final boolean isNullable;
    private final String originalJsonType;

    public JsonField(String name, String type, boolean isArray, boolean isNullable, String originalJsonType) {
        this.name = name;
        this.type = type;
        this.isArray = isArray;
        this.isNullable = isNullable;
        this.originalJsonType = originalJsonType;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getOriginalJsonType() {
        return originalJsonType;
    }

    public String getJavaType() {
        if (isArray) {
            return "List<" + type + ">";
        }
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonField jsonField = (JsonField) o;
        return isArray == jsonField.isArray &&
                isNullable == jsonField.isNullable &&
                Objects.equals(name, jsonField.name) &&
                Objects.equals(type, jsonField.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, isArray, isNullable);
    }
}