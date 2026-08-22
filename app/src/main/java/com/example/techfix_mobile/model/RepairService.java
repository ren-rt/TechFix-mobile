package com.example.techfix_mobile.model;

public class RepairService {
    private String serviceId, categoryId, name, description;
    private double price;
    private int estHours;

    public RepairService(String serviceId, String categoryId, String name,
                         String description, double price, int estHours) {
        this.serviceId = serviceId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.estHours = estHours;
    }
    public String getServiceId() { return serviceId; }
    public String getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getEstHours() { return estHours; }
}
