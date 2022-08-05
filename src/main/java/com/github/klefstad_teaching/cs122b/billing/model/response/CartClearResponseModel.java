package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.core.result.Result;

public class CartClearResponseModel {

    private Result result;

    public Result getResult() {
        return result;
    }

    public CartClearResponseModel setResult(Result result) {
        this.result = result;
        return this;
    }
}
