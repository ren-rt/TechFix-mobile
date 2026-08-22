package com.example.techfix_mobile;

public class Branch {
    private String branchId;
    private String name;
    private String address;
    private double lat;
    private double lng;
    private String phone; // Keeping for backward compatibility if needed, but adding contactNumber
    private String contactNumber;

    public Branch() {} // Required for Firestore

    public Branch(String branchId, String name, String address, double lat, double lng, String phone, String contactNumber) {
        this.branchId = branchId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.phone = phone;
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

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}