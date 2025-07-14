package com.swiftcoder.json2pojo.ui;

import com.swiftcoder.json2pojo.models.GenerationConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationDialog extends DialogWrapper {
    private JBTextArea jsonTextArea;
    private JBTextField packageNameField;
    private JBTextField classNameField;
    private JCheckBox useJacksonCheckBox;
    private JCheckBox useLombokCheckBox;
    private JCheckBox lombokGetterCheckBox;
    private JCheckBox lombokSetterCheckBox;
    private JCheckBox lombokDataCheckBox;
    private JCheckBox lombokBuilderCheckBox;
    private JCheckBox lombokNoArgsConstructorCheckBox;
    private JCheckBox lombokAllArgsConstructorCheckBox;

    private String jsonInput;
    private GenerationConfig config;
    private String defaultPackageName;

    public ConfigurationDialog(Project project) {
        this(project, "com.swiftcoder.model");
    }

    public ConfigurationDialog(Project project, String defaultPackageName) {
        super(project);
        this.defaultPackageName = defaultPackageName != null ? defaultPackageName : "com.swiftcoder.model";
        setTitle("Generate POJO from JSON");
        setModal(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // JSON Input Section
        JPanel jsonPanel = new JPanel(new BorderLayout());
        jsonPanel.setBorder(BorderFactory.createTitledBorder("JSON Input"));
        jsonTextArea = new JBTextArea(15, 50);
        jsonTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        jsonPanel.add(new JBScrollPane(jsonTextArea), BorderLayout.CENTER);

        // Configuration Section
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
        GridBagConstraints gbc = new GridBagConstraints();

        // Package Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        configPanel.add(new JLabel("Package Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        packageNameField = new JBTextField(defaultPackageName, 20);
        configPanel.add(packageNameField, gbc);

        // Class Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        configPanel.add(new JLabel("Root Class Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        classNameField = new JBTextField("RootClass", 20);
        configPanel.add(classNameField, gbc);

        // Jackson Options
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        useJacksonCheckBox = new JCheckBox("Use Jackson Annotations");
        configPanel.add(useJacksonCheckBox, gbc);

        // Lombok Options
        gbc.gridx = 0; gbc.gridy = 3;
        useLombokCheckBox = new JCheckBox("Use Lombok Annotations");
        configPanel.add(useLombokCheckBox, gbc);

        // Lombok Annotation Selection
        JPanel lombokPanel = new JPanel(new GridLayout(3, 2));
        lombokPanel.setBorder(BorderFactory.createTitledBorder("Lombok Annotations"));
        
        lombokGetterCheckBox = new JCheckBox("@Getter");
        lombokSetterCheckBox = new JCheckBox("@Setter");
        lombokDataCheckBox = new JCheckBox("@Data");
        lombokBuilderCheckBox = new JCheckBox("@Builder");
        lombokNoArgsConstructorCheckBox = new JCheckBox("@NoArgsConstructor");
        lombokAllArgsConstructorCheckBox = new JCheckBox("@AllArgsConstructor");

        lombokPanel.add(lombokGetterCheckBox);
        lombokPanel.add(lombokSetterCheckBox);
        lombokPanel.add(lombokDataCheckBox);
        lombokPanel.add(lombokBuilderCheckBox);
        lombokPanel.add(lombokNoArgsConstructorCheckBox);
        lombokPanel.add(lombokAllArgsConstructorCheckBox);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        configPanel.add(lombokPanel, gbc);

        // Add action listeners
        useLombokCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = useLombokCheckBox.isSelected();
                lombokGetterCheckBox.setEnabled(enabled);
                lombokSetterCheckBox.setEnabled(enabled);
                lombokDataCheckBox.setEnabled(enabled);
                lombokBuilderCheckBox.setEnabled(enabled);
                lombokNoArgsConstructorCheckBox.setEnabled(enabled);
                lombokAllArgsConstructorCheckBox.setEnabled(enabled);
            }
        });

        // Initially disable Lombok options
        lombokGetterCheckBox.setEnabled(false);
        lombokSetterCheckBox.setEnabled(false);
        lombokDataCheckBox.setEnabled(false);
        lombokBuilderCheckBox.setEnabled(false);
        lombokNoArgsConstructorCheckBox.setEnabled(false);
        lombokAllArgsConstructorCheckBox.setEnabled(false);

        // Set default values
        useJacksonCheckBox.setSelected(true);
        lombokGetterCheckBox.setSelected(true);
        lombokSetterCheckBox.setSelected(true);

        panel.add(jsonPanel, BorderLayout.CENTER);
        panel.add(configPanel, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    protected void doOKAction() {
        jsonInput = jsonTextArea.getText().trim();
        
        if (jsonInput.isEmpty()) {
            setErrorText("Please enter JSON input");
            return;
        }

        Set<String> lombokAnnotations = new HashSet<>();
        if (useLombokCheckBox.isSelected()) {
            if (lombokGetterCheckBox.isSelected()) lombokAnnotations.add("@Getter");
            if (lombokSetterCheckBox.isSelected()) lombokAnnotations.add("@Setter");
            if (lombokDataCheckBox.isSelected()) lombokAnnotations.add("@Data");
            if (lombokBuilderCheckBox.isSelected()) lombokAnnotations.add("@Builder");
            if (lombokNoArgsConstructorCheckBox.isSelected()) lombokAnnotations.add("@NoArgsConstructor");
            if (lombokAllArgsConstructorCheckBox.isSelected()) lombokAnnotations.add("@AllArgsConstructor");
        }

        config = new GenerationConfig(
            useJacksonCheckBox.isSelected(),
            useLombokCheckBox.isSelected(),
            lombokAnnotations,
            packageNameField.getText().trim(),
            classNameField.getText().trim()
        );

        super.doOKAction();
    }

    public String getJsonInput() {
        return jsonInput;
    }

    public GenerationConfig getConfig() {
        return config;
    }
}