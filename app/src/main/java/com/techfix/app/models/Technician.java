package com.techfix.app.models;

public class Technician {
    private String technicianId;
    private String name;
    private String branchId;
    private String specialization;
    private boolean isAvailable;

    public Technician() {}

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
