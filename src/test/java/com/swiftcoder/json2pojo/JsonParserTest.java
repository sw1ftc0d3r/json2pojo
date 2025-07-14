package com.swiftcoder.json2pojo;

import com.swiftcoder.json2pojo.models.JsonClass;
import com.swiftcoder.json2pojo.models.JsonField;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class JsonParserTest {

    @Test
    public void testSimpleJsonParsing() throws IOException {
        String json = "{\"name\":\"John\",\"age\":30,\"active\":true}";
        JsonParser parser = new JsonParser();
        JsonClass result = parser.parseJson(json, "Person");

        assertEquals("Person", result.getClassName());
        assertEquals(3, result.getFields().size());

        JsonField nameField = result.getFields().stream()
            .filter(f -> f.getName().equals("name"))
            .findFirst()
            .orElse(null);
        assertNotNull(nameField);
        assertEquals("String", nameField.getType());
        assertFalse(nameField.isArray());

        JsonField ageField = result.getFields().stream()
            .filter(f -> f.getName().equals("age"))
            .findFirst()
            .orElse(null);
        assertNotNull(ageField);
        assertEquals("Integer", ageField.getType());
        assertFalse(ageField.isArray());

        JsonField activeField = result.getFields().stream()
            .filter(f -> f.getName().equals("active"))
            .findFirst()
            .orElse(null);
        assertNotNull(activeField);
        assertEquals("Boolean", activeField.getType());
        assertFalse(activeField.isArray());
    }

    @Test
    public void testNestedObjectParsing() throws IOException {
        String json = "{\"user\":{\"name\":\"John\",\"age\":30},\"count\":5}";
        JsonParser parser = new JsonParser();
        JsonClass result = parser.parseJson(json, "Response");

        assertEquals("Response", result.getClassName());
        assertEquals(2, result.getFields().size());
        assertEquals(1, result.getNestedClasses().size());

        JsonClass userClass = result.getNestedClasses().get(0);
        assertEquals("User", userClass.getClassName());
        assertEquals(2, userClass.getFields().size());
    }

    @Test
    public void testArrayParsing() throws IOException {
        String json = "{\"items\":[\"apple\",\"banana\"],\"numbers\":[1,2,3]}";
        JsonParser parser = new JsonParser();
        JsonClass result = parser.parseJson(json, "Container");

        assertEquals("Container", result.getClassName());
        assertEquals(2, result.getFields().size());

        JsonField itemsField = result.getFields().stream()
            .filter(f -> f.getName().equals("items"))
            .findFirst()
            .orElse(null);
        assertNotNull(itemsField);
        assertEquals("String", itemsField.getType());
        assertTrue(itemsField.isArray());

        JsonField numbersField = result.getFields().stream()
            .filter(f -> f.getName().equals("numbers"))
            .findFirst()
            .orElse(null);
        assertNotNull(numbersField);
        assertEquals("Integer", numbersField.getType());
        assertTrue(numbersField.isArray());
    }
}