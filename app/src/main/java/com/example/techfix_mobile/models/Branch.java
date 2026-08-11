package com.example.techfix_mobile.models;

public class Branch {
    private String branchId;
    private String name;
    private String address;
    private double lat;
    private double lng;
    private String contactNumber;

    public Branch() {} // required empty constructor for Firestore deserialization

    public Branch(String branchId, String name, String address, double lat, double lng, String contactNumber) {
        this.branchId = branchId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.contactNumber = contactNumber;
    }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}