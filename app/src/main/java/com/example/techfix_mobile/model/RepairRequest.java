package com.example.techfix_mobile.model;

public class RepairRequest {
    private String requestId, serviceId, categoryId, deviceDetails, issueDesc;
    private String photoLocalPath, photoUrl, branchId, technicianId, status, syncStatus;
    private double customerLat, customerLng;
    private long requestedAt, completedAt;

    public RepairRequest(String requestId, String serviceId, String categoryId,
                         String deviceDetails, String issueDesc,
                         String photoLocalPath, String photoUrl,
                         String branchId, String technicianId, String status,
                         double customerLat, double customerLng,
                         long requestedAt, long completedAt, String syncStatus) {
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.categoryId = categoryId;
        this.deviceDetails = deviceDetails;
        this.issueDesc = issueDesc;
        this.photoLocalPath = photoLocalPath;
        this.photoUrl = photoUrl;
        this.branchId = branchId;
        this.technicianId = technicianId;
        this.status = status;
        this.customerLat = customerLat;
        this.customerLng = customerLng;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.syncStatus = syncStatus;
    }

    public String getRequestId() { return requestId; }
    public String getServiceId() { return serviceId; }
    public String getCategoryId() { return categoryId; }
    public String getDeviceDetails() { return deviceDetails; }
    public String getIssueDesc() { return issueDesc; }
    public String getPhotoLocalPath() { return photoLocalPath; }
    public String getPhotoUrl() { return photoUrl; }
    public String getBranchId() { return branchId; }
    public String getTechnicianId() { return technicianId; }
    public String getStatus() { return status; }
    public double getCustomerLat() { return customerLat; }
    public double getCustomerLng() { return customerLng; }
    public long getRequestedAt() { return requestedAt; }
    public long getCompletedAt() { return completedAt; }
    public String getSyncStatus() { return syncStatus; }

    public void setStatus(String status) { this.status = status; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}