package com.github.klefstad_teaching.cs122b.billing.model.request;

public class OrderCompleteRequestModel {

    private String paymentIntentId;

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public OrderCompleteRequestModel setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
        return this;
    }
}
