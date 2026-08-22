package com.example.techfix_mobile.models;

public class SparePart {
    private String partId;
    private String name;
    private String categoryId;
    private String branchId;
    private int stockQty;
    private double unitPrice;

    public SparePart() {}

    public String getPartId() { return partId; }
    public void setPartId(String partId) { this.partId = partId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
