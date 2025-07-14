package com.swiftcoder.json2pojo.generators;

import com.swiftcoder.json2pojo.models.GenerationConfig;
import com.swiftcoder.json2pojo.models.JsonClass;
import com.swiftcoder.json2pojo.models.JsonField;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JavaCodeGenerator {
    
    public List<String> generateJavaClasses(JsonClass rootClass, GenerationConfig config) {
        List<String> generatedClasses = new ArrayList<>();
        
        // Generate root class
        String rootClassCode = generateSingleClass(rootClass, config);
        generatedClasses.add(rootClassCode);
        
        // Generate nested classes
        generateNestedClasses(rootClass, config, generatedClasses);
        
        return generatedClasses;
    }
    
    private void generateNestedClasses(JsonClass parentClass, GenerationConfig config, List<String> generatedClasses) {
        for (JsonClass nestedClass : parentClass.getNestedClasses()) {
            String nestedClassCode = generateSingleClass(nestedClass, config);
            generatedClasses.add(nestedClassCode);
            
            // Recursively generate nested classes
            generateNestedClasses(nestedClass, config, generatedClasses);
        }
    }
    
    private String generateSingleClass(JsonClass jsonClass, GenerationConfig config) {
        StringBuilder code = new StringBuilder();
        
        // Package declaration
        code.append("package ").append(config.getPackageName()).append(";\n\n");
        
        // Imports
        addImports(code, config, jsonClass);
        
        // Class annotations
        addClassAnnotations(code, config);
        
        // Class declaration
        code.append("public class ").append(jsonClass.getClassName()).append(" {\n");
        
        // Fields
        for (JsonField field : jsonClass.getFields()) {
            addField(code, field, config);
        }
        
        // Generate getters and setters if not using Lombok
        if (!config.isUseLombok() || !hasGetterSetterAnnotation(config)) {
            generateGettersAndSetters(code, jsonClass);
        }
        
        // Generate constructors if not using Lombok
        if (!config.isUseLombok() || !hasConstructorAnnotation(config)) {
            generateConstructors(code, jsonClass);
        }
        
        code.append("}\n");
        
        return code.toString();
    }
    
    private void addImports(StringBuilder code, GenerationConfig config, JsonClass jsonClass) {
        Set<String> imports = new java.util.HashSet<>();
        
        // Jackson imports
        if (config.isUseJackson()) {
            imports.add("import com.fasterxml.jackson.annotation.JsonProperty;");
        }
        
        // Lombok imports
        if (config.isUseLombok()) {
            Set<String> lombokAnnotations = config.getLombokAnnotations();
            if (lombokAnnotations.contains("@Getter")) {
                imports.add("import lombok.Getter;");
            }
            if (lombokAnnotations.contains("@Setter")) {
                imports.add("import lombok.Setter;");
            }
            if (lombokAnnotations.contains("@Data")) {
                imports.add("import lombok.Data;");
            }
            if (lombokAnnotations.contains("@Builder")) {
                imports.add("import lombok.Builder;");
            }
            if (lombokAnnotations.contains("@NoArgsConstructor")) {
                imports.add("import lombok.NoArgsConstructor;");
            }
            if (lombokAnnotations.contains("@AllArgsConstructor")) {
                imports.add("import lombok.AllArgsConstructor;");
            }
        }
        
        // List import if needed
        for (JsonField field : jsonClass.getFields()) {
            if (field.isArray()) {
                imports.add("import java.util.List;");
                break;
            }
        }
        
        // Add imports to code
        for (String importStatement : imports) {
            code.append(importStatement).append("\n");
        }
        
        if (!imports.isEmpty()) {
            code.append("\n");
        }
    }
    
    private void addClassAnnotations(StringBuilder code, GenerationConfig config) {
        if (config.isUseLombok()) {
            Set<String> lombokAnnotations = config.getLombokAnnotations();
            for (String annotation : lombokAnnotations) {
                code.append(annotation).append("\n");
            }
        }
    }
    
    private void addField(StringBuilder code, JsonField field, GenerationConfig config) {
        // Field annotation
        if (config.isUseJackson()) {
            code.append("    @JsonProperty(\"").append(field.getName()).append("\")\n");
        }
        
        // Field declaration
        code.append("    private ").append(field.getJavaType()).append(" ").append(field.getName()).append(";\n\n");
    }
    
    private void generateGettersAndSetters(StringBuilder code, JsonClass jsonClass) {
        for (JsonField field : jsonClass.getFields()) {
            // Getter
            String capitalizedName = capitalizeFirstLetter(field.getName());
            code.append("    public ").append(field.getJavaType()).append(" get").append(capitalizedName).append("() {\n");
            code.append("        return ").append(field.getName()).append(";\n");
            code.append("    }\n\n");
            
            // Setter
            code.append("    public void set").append(capitalizedName).append("(").append(field.getJavaType()).append(" ").append(field.getName()).append(") {\n");
            code.append("        this.").append(field.getName()).append(" = ").append(field.getName()).append(";\n");
            code.append("    }\n\n");
        }
    }
    
    private void generateConstructors(StringBuilder code, JsonClass jsonClass) {
        // Default constructor
        code.append("    public ").append(jsonClass.getClassName()).append("() {\n");
        code.append("    }\n\n");
        
        // Constructor with all fields
        if (!jsonClass.getFields().isEmpty()) {
            code.append("    public ").append(jsonClass.getClassName()).append("(");
            
            for (int i = 0; i < jsonClass.getFields().size(); i++) {
                JsonField field = jsonClass.getFields().get(i);
                code.append(field.getJavaType()).append(" ").append(field.getName());
                if (i < jsonClass.getFields().size() - 1) {
                    code.append(", ");
                }
            }
            
            code.append(") {\n");
            
            for (JsonField field : jsonClass.getFields()) {
                code.append("        this.").append(field.getName()).append(" = ").append(field.getName()).append(";\n");
            }
            
            code.append("    }\n\n");
        }
    }
    
    private boolean hasGetterSetterAnnotation(GenerationConfig config) {
        Set<String> annotations = config.getLombokAnnotations();
        return annotations.contains("@Getter") || annotations.contains("@Setter") || annotations.contains("@Data");
    }
    
    private boolean hasConstructorAnnotation(GenerationConfig config) {
        Set<String> annotations = config.getLombokAnnotations();
        return annotations.contains("@NoArgsConstructor") || annotations.contains("@AllArgsConstructor");
    }
    
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}