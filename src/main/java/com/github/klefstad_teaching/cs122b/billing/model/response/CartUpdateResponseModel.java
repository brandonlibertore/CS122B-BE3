package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.core.result.Result;

public class CartUpdateResponseModel {

    private Result result;

    public Result getResult() {
        return result;
    }

    public CartUpdateResponseModel setResult(Result result) {
        this.result = result;
        return this;
    }
}
