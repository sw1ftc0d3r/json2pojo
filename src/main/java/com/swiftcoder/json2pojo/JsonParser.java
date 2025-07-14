package com.swiftcoder.json2pojo;

import com.swiftcoder.json2pojo.models.JsonClass;
import com.swiftcoder.json2pojo.models.JsonField;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class JsonParser {
    private final ObjectMapper objectMapper;

    public JsonParser() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonClass parseJson(String json, String rootClassName) throws IOException {
        JsonNode rootNode = objectMapper.readTree(json);
        return parseJsonNode(rootNode, rootClassName);
    }

    private JsonClass parseJsonNode(JsonNode node, String className) {
        JsonClass jsonClass = new JsonClass(capitalizeFirstLetter(className));

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();

                JsonField jsonField = createJsonField(fieldName, fieldValue, jsonClass);
                jsonClass.addField(jsonField);
            }
        }

        return jsonClass;
    }

    private JsonField createJsonField(String fieldName, JsonNode fieldValue, JsonClass parentClass) {
        if (fieldValue.isNull()) {
            return new JsonField(fieldName, "Object", false, true, "null");
        }

        if (fieldValue.isArray()) {
            if (fieldValue.size() == 0) {
                return new JsonField(fieldName, "Object", true, false, "array");
            }
            
            JsonNode firstElement = fieldValue.get(0);
            if (firstElement.isObject()) {
                String nestedClassName = capitalizeFirstLetter(fieldName);
                if (nestedClassName.endsWith("s")) {
                    nestedClassName = nestedClassName.substring(0, nestedClassName.length() - 1);
                }
                JsonClass nestedClass = parseJsonNode(firstElement, nestedClassName);
                parentClass.addNestedClass(nestedClass);
                return new JsonField(fieldName, nestedClassName, true, false, "array");
            } else {
                String elementType = getJavaType(firstElement);
                return new JsonField(fieldName, elementType, true, false, "array");
            }
        }

        if (fieldValue.isObject()) {
            String nestedClassName = capitalizeFirstLetter(fieldName);
            JsonClass nestedClass = parseJsonNode(fieldValue, nestedClassName);
            parentClass.addNestedClass(nestedClass);
            return new JsonField(fieldName, nestedClassName, false, false, "object");
        }

        String javaType = getJavaType(fieldValue);
        return new JsonField(fieldName, javaType, false, false, fieldValue.getNodeType().toString());
    }

    private String getJavaType(JsonNode node) {
        if (node.isBoolean()) {
            return "Boolean";
        }
        if (node.isInt()) {
            return "Integer";
        }
        if (node.isLong()) {
            return "Long";
        }
        if (node.isDouble() || node.isFloat()) {
            return "Double";
        }
        if (node.isTextual()) {
            return "String";
        }
        return "Object";
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}