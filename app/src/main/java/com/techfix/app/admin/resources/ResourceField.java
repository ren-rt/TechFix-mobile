package com.techfix.app.admin.resources;

public class ResourceField {
    public final String key;
    public final String label;
    public final FieldType type;

    public ResourceField(String key, String label, FieldType type) {
        this.key = key;
        this.label = label;
        this.type = type;
    }
}