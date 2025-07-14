package com.swiftcoder.json2pojo.models;

import java.util.ArrayList;
import java.util.List;

public class JsonClass {
    private final String className;
    private final List<JsonField> fields;
    private final List<JsonClass> nestedClasses;

    public JsonClass(String className) {
        this.className = className;
        this.fields = new ArrayList<>();
        this.nestedClasses = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public List<JsonField> getFields() {
        return fields;
    }

    public List<JsonClass> getNestedClasses() {
        return nestedClasses;
    }

    public void addField(JsonField field) {
        fields.add(field);
    }

    public void addNestedClass(JsonClass nestedClass) {
        nestedClasses.add(nestedClass);
    }

    public boolean hasNestedClasses() {
        return !nestedClasses.isEmpty();
    }
}