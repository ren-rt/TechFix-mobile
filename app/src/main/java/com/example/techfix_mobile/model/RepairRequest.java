package com.example.techfix_mobile.model;

public class RepairRequest {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ASSIGNED = "assigned";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_READY_FOR_PICKUP = "ready_for_pickup";

    private String requestId;
    private String customerId;
    private String serviceId;
    private String categoryId;
    private String deviceDetails;
    private String issueDesc;
    private String devicePhotoUrl;
    private String assignedBranchId;
    private String assignedTechnicianId;
    private String status;
    private double customerLat;
    private double customerLng;
    private long requestedAt;
    private long completedAt;
    private String photoLocalPath;

    private String syncStatus = "synced";

    public RepairRequest() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getDeviceDetails() { return deviceDetails; }
    public void setDeviceDetails(String deviceDetails) { this.deviceDetails = deviceDetails; }
    public String getIssueDesc() { return issueDesc; }
    public void setIssueDesc(String issueDesc) { this.issueDesc = issueDesc; }
    public String getDevicePhotoUrl() { return devicePhotoUrl; }
    public void setDevicePhotoUrl(String devicePhotoUrl) { this.devicePhotoUrl = devicePhotoUrl; }
    public String getAssignedBranchId() { return assignedBranchId; }
    public void setAssignedBranchId(String assignedBranchId) { this.assignedBranchId = assignedBranchId; }
    public String getAssignedTechnicianId() { return assignedTechnicianId; }
    public void setAssignedTechnicianId(String assignedTechnicianId) { this.assignedTechnicianId = assignedTechnicianId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getCustomerLat() { return customerLat; }
    public void setCustomerLat(double customerLat) { this.customerLat = customerLat; }
    public double getCustomerLng() { return customerLng; }
    public void setCustomerLng(double customerLng) { this.customerLng = customerLng; }
    public long getRequestedAt() { return requestedAt; }
    public void setRequestedAt(long requestedAt) { this.requestedAt = requestedAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public String getPhotoLocalPath() { return photoLocalPath; }
    public void setPhotoLocalPath(String photoLocalPath) { this.photoLocalPath = photoLocalPath; }

    public boolean isPayable() {
        return STATUS_COMPLETED.equals(status) || STATUS_READY_FOR_PICKUP.equals(status);
    }
}