package com.example.techfix_mobile.model;

public class DeviceCategory {
    private String categoryId, name;

    public DeviceCategory(String categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public String getCategoryId() { return categoryId; }
    public String getName() { return name; }
}