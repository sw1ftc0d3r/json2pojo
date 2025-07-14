package com.swiftcoder.json2pojo.actions;

import com.swiftcoder.json2pojo.JsonParser;
import com.swiftcoder.json2pojo.generators.JavaCodeGenerator;
import com.swiftcoder.json2pojo.models.GenerationConfig;
import com.swiftcoder.json2pojo.models.JsonClass;
import com.swiftcoder.json2pojo.ui.ConfigurationDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileEditor.FileEditorManager;

import java.io.IOException;
import java.util.List;

public class GeneratePojoAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Show configuration dialog
        ConfigurationDialog dialog = new ConfigurationDialog(project);
        if (!dialog.showAndGet()) {
            return;
        }

        String jsonInput = dialog.getJsonInput();
        GenerationConfig config = dialog.getConfig();

        try {
            // Parse JSON
            JsonParser parser = new JsonParser();
            JsonClass rootClass = parser.parseJson(jsonInput, config.getRootClassName());

            // Generate Java code
            JavaCodeGenerator generator = new JavaCodeGenerator();
            List<String> generatedClasses = generator.generateJavaClasses(rootClass, config);

            // Create files
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    createJavaFiles(project, generatedClasses, config, rootClass);
                    Messages.showInfoMessage(project, 
                        "Successfully generated " + generatedClasses.size() + " Java class(es)!", 
                        "Generation Complete");
                } catch (Exception ex) {
                    Messages.showErrorDialog(project, 
                        "Error creating Java files: " + ex.getMessage(), 
                        "Generation Error");
                }
            });

        } catch (IOException ex) {
            Messages.showErrorDialog(project, 
                "Error parsing JSON: " + ex.getMessage(), 
                "JSON Parse Error");
        } catch (Exception ex) {
            Messages.showErrorDialog(project, 
                "Unexpected error: " + ex.getMessage(), 
                "Error");
        }
    }

    private void createJavaFiles(Project project, List<String> generatedClasses, 
                                GenerationConfig config, JsonClass rootClass) {
        // Get the current directory or use src/main/java
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) {
            throw new RuntimeException("Could not find project base directory");
        }

        // Try to find src/main/java directory
        VirtualFile srcDir = baseDir.findFileByRelativePath("src/main/java");
        if (srcDir == null) {
            // If src/main/java doesn't exist, create files in the project root
            srcDir = baseDir;
        }

        // Create package directories
        String[] packageParts = config.getPackageName().split("\\.");
        VirtualFile packageDir = srcDir;
        
        for (String part : packageParts) {
            VirtualFile nextDir = packageDir.findChild(part);
            if (nextDir == null) {
                try {
                    nextDir = packageDir.createChildDirectory(this, part);
                } catch (IOException e) {
                    throw new RuntimeException("Could not create package directory: " + part, e);
                }
            }
            packageDir = nextDir;
        }

        // Create Java files
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiDirectory psiPackageDir = psiManager.findDirectory(packageDir);
        
        if (psiPackageDir == null) {
            throw new RuntimeException("Could not find package directory");
        }

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        
        for (int i = 0; i < generatedClasses.size(); i++) {
            String classCode = generatedClasses.get(i);
            
            // Extract class name from code
            String className = extractClassName(classCode);
            if (className == null) {
                if (i == 0) {
                    className = config.getRootClassName();
                } else {
                    className = "GeneratedClass" + i;
                }
            }
            
            String fileName = className + ".java";
            
            // Create the file
            PsiFile javaFile = fileFactory.createFileFromText(fileName, classCode);
            PsiFile addedFile = (PsiFile) psiPackageDir.add(javaFile);
            
            // Open the first (root) class file in the editor
            if (i == 0) {
                FileEditorManager.getInstance(project).openFile(addedFile.getVirtualFile(), true);
            }
        }
    }

    private String extractClassName(String classCode) {
        // Simple regex to extract class name
        String[] lines = classCode.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("public class ")) {
                String[] parts = line.split("\\s+");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("class".equals(parts[i])) {
                        return parts[i + 1];
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void update(AnActionEvent e) {
        // Enable action only when a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}