package com.techfix.app.admin.resources;

import java.util.Arrays;
import java.util.List;

public enum ResourceType {
    BRANCH("branches", "branchId", "Branch", Arrays.asList(
            new ResourceField("name", "Branch Name", FieldType.TEXT),
            new ResourceField("address", "Address", FieldType.TEXT),
            new ResourceField("latlng", "Location", FieldType.LATLNG),
            new ResourceField("contactNumber", "Contact Number", FieldType.TEXT)
    )),
    CATEGORY("deviceCategories", "categoryId", "Device Category", Arrays.asList(
            new ResourceField("name", "Category Name", FieldType.TEXT)
    )),
    SERVICE("repairServices", "serviceId", "Repair Service", Arrays.asList(
            new ResourceField("categoryId", "Device Category", FieldType.CATEGORY_DROPDOWN),
            new ResourceField("name", "Service Name", FieldType.TEXT),
            new ResourceField("description", "Description", FieldType.TEXT),
            new ResourceField("basePrice", "Base Price (LKR)", FieldType.NUMBER_DECIMAL),
            new ResourceField("estTimeHrs", "Estimated Time (hrs)", FieldType.NUMBER_INT)
    )),
    PART("spareParts", "partId", "Spare Part", Arrays.asList(
            new ResourceField("name", "Part Name", FieldType.TEXT),
            new ResourceField("categoryId", "Device Category", FieldType.CATEGORY_DROPDOWN),
            new ResourceField("branchId", "Branch", FieldType.BRANCH_DROPDOWN),
            new ResourceField("stockQty", "Stock Quantity", FieldType.NUMBER_INT),
            new ResourceField("unitPrice", "Unit Price (LKR)", FieldType.NUMBER_DECIMAL)
    )),
    TECHNICIAN("technicians", "technicianId", "Technician", Arrays.asList(
            new ResourceField("name", "Technician Name", FieldType.TEXT),
            new ResourceField("branchId", "Branch", FieldType.BRANCH_DROPDOWN),
            new ResourceField("specialization", "Specialization", FieldType.TEXT),
            new ResourceField("isAvailable", "Available", FieldType.BOOLEAN)
    ));

    public final String collectionName;
    public final String idFieldKey;
    public final String displayName;
    public final List<ResourceField> fields;

    ResourceType(String collectionName, String idFieldKey, String displayName, List<ResourceField> fields) {
        this.collectionName = collectionName;
        this.idFieldKey = idFieldKey;
        this.displayName = displayName;
        this.fields = fields;
    }

    public String getTitleFieldKey() {
        for (ResourceField f : fields) {
            if (f.type == FieldType.TEXT) return f.key;
        }
        return fields.get(0).key;
    }
}