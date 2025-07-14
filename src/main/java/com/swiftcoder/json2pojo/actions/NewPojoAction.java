package com.swiftcoder.json2pojo.actions;

import com.swiftcoder.json2pojo.JsonParser;
import com.swiftcoder.json2pojo.generators.JavaCodeGenerator;
import com.swiftcoder.json2pojo.models.GenerationConfig;
import com.swiftcoder.json2pojo.models.JsonClass;
import com.swiftcoder.json2pojo.ui.ConfigurationDialog;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;

import java.io.IOException;
import java.util.List;

public class NewPojoAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Get the selected directory
        PsiDirectory selectedDirectory = getSelectedDirectory(e);
        if (selectedDirectory == null) {
            Messages.showErrorDialog(project, "Please select a directory", "No Directory Selected");
            return;
        }

        // Detect package name from selected directory
        String packageName = detectPackageName(project, selectedDirectory);

        // Show configuration dialog with pre-populated package name
        ConfigurationDialog dialog = new ConfigurationDialog(project, packageName);
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

            // Create files in the selected directory
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    createJavaFilesInDirectory(project, selectedDirectory, generatedClasses, config);
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

    private PsiDirectory getSelectedDirectory(AnActionEvent e) {
        // Try to get the selected directory from the context
        PsiDirectory directory = e.getData(LangDataKeys.IDE_VIEW) != null ? 
            e.getData(LangDataKeys.IDE_VIEW).getOrChooseDirectory() : null;
        
        if (directory == null) {
            // Fallback: try to get from PSI element
            VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
            if (selectedFiles != null && selectedFiles.length > 0) {
                VirtualFile selectedFile = selectedFiles[0];
                Project project = e.getProject();
                if (project != null) {
                    PsiManager psiManager = PsiManager.getInstance(project);
                    if (selectedFile.isDirectory()) {
                        directory = psiManager.findDirectory(selectedFile);
                    } else {
                        directory = psiManager.findDirectory(selectedFile.getParent());
                    }
                }
            }
        }
        
        return directory;
    }

    private String detectPackageName(Project project, PsiDirectory directory) {
        try {
            // Get the package for the directory using JavaDirectoryService
            PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
            if (psiPackage != null) {
                String packageName = psiPackage.getQualifiedName();
                if (packageName != null && !packageName.isEmpty()) {
                    return packageName;
                }
            }
        } catch (Exception e) {
            // Fallback to manual detection
        }

        // Fallback: try to detect package from directory structure
        return detectPackageFromPath(project, directory);
    }

    private String detectPackageFromPath(Project project, PsiDirectory directory) {
        VirtualFile virtualFile = directory.getVirtualFile();
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        
        // Get the source root
        VirtualFile sourceRoot = fileIndex.getSourceRootForFile(virtualFile);
        if (sourceRoot == null) {
            return "com.swiftcoder.model"; // Default package
        }

        // Calculate relative path from source root
        String relativePath = virtualFile.getPath().substring(sourceRoot.getPath().length());
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        // Convert path to package name
        String packageName = relativePath.replace("/", ".");
        
        // Return default if empty
        return packageName.isEmpty() ? "com.swiftcoder.model" : packageName;
    }

    private void createJavaFilesInDirectory(Project project, PsiDirectory targetDirectory, 
                                          List<String> generatedClasses, GenerationConfig config) {
        
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
            PsiFile addedFile = (PsiFile) targetDirectory.add(javaFile);
            
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
        Project project = e.getProject();
        PsiDirectory selectedDirectory = getSelectedDirectory(e);
        
        // Enable action only when a project is open and a directory is selected
        boolean enabled = project != null && selectedDirectory != null;
        e.getPresentation().setEnabledAndVisible(enabled);
    }
}