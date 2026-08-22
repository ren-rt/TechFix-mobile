package com.example.techfix_mobile.model;

public class Branch {
    private String branchId, name, address;
    private double lat, lng;

    public Branch(String branchId, String name, String address, double lat, double lng) {
        this.branchId = branchId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getBranchId() { return branchId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}