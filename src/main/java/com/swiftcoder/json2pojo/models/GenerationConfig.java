package com.swiftcoder.json2pojo.models;

import java.util.Set;

public class GenerationConfig {
    private final boolean useJackson;
    private final boolean useLombok;
    private final Set<String> lombokAnnotations;
    private final String packageName;
    private final String rootClassName;

    public GenerationConfig(boolean useJackson, boolean useLombok, Set<String> lombokAnnotations, 
                           String packageName, String rootClassName) {
        this.useJackson = useJackson;
        this.useLombok = useLombok;
        this.lombokAnnotations = lombokAnnotations;
        this.packageName = packageName;
        this.rootClassName = rootClassName;
    }

    public boolean isUseJackson() {
        return useJackson;
    }

    public boolean isUseLombok() {
        return useLombok;
    }

    public Set<String> getLombokAnnotations() {
        return lombokAnnotations;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getRootClassName() {
        return rootClassName;
    }
}