package com.example.techfix_mobile.model;

public class RepairRequest {
    private String requestId, customerId, serviceId, categoryId, deviceDetails, issueDesc;
    private String devicePhotoUrl, assignedBranchId, assignedTechnicianId, status;
    private String photoLocalPath;   // local-only, not written to Firestore
    private String syncStatus;       // local-only, not written to Firestore
    private double customerLat, customerLng;
    private long requestedAt, completedAt;

    public RepairRequest(String requestId, String customerId, String serviceId, String categoryId,
                         String deviceDetails, String issueDesc,
                         String photoLocalPath, String devicePhotoUrl,
                         String assignedBranchId, String assignedTechnicianId, String status,
                         double customerLat, double customerLng,
                         long requestedAt, long completedAt, String syncStatus) {
        this.requestId = requestId;
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.categoryId = categoryId;
        this.deviceDetails = deviceDetails;
        this.issueDesc = issueDesc;
        this.photoLocalPath = photoLocalPath;
        this.devicePhotoUrl = devicePhotoUrl;
        this.assignedBranchId = assignedBranchId;
        this.assignedTechnicianId = assignedTechnicianId;
        this.status = status;
        this.customerLat = customerLat;
        this.customerLng = customerLng;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.syncStatus = syncStatus;
    }

    public String getRequestId() { return requestId; }
    public String getCustomerId() { return customerId; }
    public String getServiceId() { return serviceId; }
    public String getCategoryId() { return categoryId; }
    public String getDeviceDetails() { return deviceDetails; }
    public String getIssueDesc() { return issueDesc; }
    public String getPhotoLocalPath() { return photoLocalPath; }
    public String getDevicePhotoUrl() { return devicePhotoUrl; }
    public String getAssignedBranchId() { return assignedBranchId; }
    public String getAssignedTechnicianId() { return assignedTechnicianId; }
    public String getStatus() { return status; }
    public double getCustomerLat() { return customerLat; }
    public double getCustomerLng() { return customerLng; }
    public long getRequestedAt() { return requestedAt; }
    public long getCompletedAt() { return completedAt; }
    public String getSyncStatus() { return syncStatus; }

    public void setStatus(String status) { this.status = status; }
    public void setAssignedBranchId(String assignedBranchId) { this.assignedBranchId = assignedBranchId; }
    public void setAssignedTechnicianId(String assignedTechnicianId) { this.assignedTechnicianId = assignedTechnicianId; }
    public void setDevicePhotoUrl(String devicePhotoUrl) { this.devicePhotoUrl = devicePhotoUrl; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}