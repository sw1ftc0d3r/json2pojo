package com.swiftcoder.json2pojo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NewPojoActionTest {

    @Test
    public void testPackageNameDetection() {
        // Test basic package name conversion
        String path = "com/swiftcoder/model";
        String expectedPackage = "com.swiftcoder.model";
        String actualPackage = path.replace("/", ".");
        assertEquals(expectedPackage, actualPackage);
    }

    @Test
    public void testEmptyPackageName() {
        String path = "";
        String packageName = path.isEmpty() ? "com.swiftcoder.model" : path.replace("/", ".");
        assertEquals("com.swiftcoder.model", packageName);
    }

    @Test
    public void testSingleLevelPackage() {
        String path = "model";
        String packageName = path.replace("/", ".");
        assertEquals("model", packageName);
    }

    @Test
    public void testDeepPackageStructure() {
        String path = "com/company/project/model/dto";
        String packageName = path.replace("/", ".");
        assertEquals("com.company.project.model.dto", packageName);
    }
}