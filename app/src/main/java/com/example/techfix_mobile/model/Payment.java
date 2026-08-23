package com.example.techfix_mobile.model;

public class Payment {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";

    private String paymentId; // = order_id sent to PayHere
    private String requestId;
    private String customerId;
    private double amount;
    private String currency = "LKR";
    private String status = STATUS_PENDING;
    private String payherePaymentId;
    private String method;
    private long paidAt;

    public Payment() {}

    public Payment(String paymentId, String requestId, String customerId, double amount) {
        this.paymentId = paymentId;
        this.requestId = requestId;
        this.customerId = customerId;
        this.amount = amount;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPayherePaymentId() { return payherePaymentId; }
    public void setPayherePaymentId(String payherePaymentId) { this.payherePaymentId = payherePaymentId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public long getPaidAt() { return paidAt; }
    public void setPaidAt(long paidAt) { this.paidAt = paidAt; }
}
