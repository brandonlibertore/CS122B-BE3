package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.core.result.Result;

public class OrderPaymentResponseMode {

    private Result result;
    private String paymentIntentId;
    private String clientSecret;

    public Result getResult() {
        return result;
    }

    public OrderPaymentResponseMode setResult(Result result) {
        this.result = result;
        return this;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public OrderPaymentResponseMode setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public OrderPaymentResponseMode setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }
}
