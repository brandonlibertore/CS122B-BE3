package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.core.result.Result;

public class CartInsertResponseModel {

    private Result result;

    public Result getResult() {
        return result;
    }

    public CartInsertResponseModel setResult(Result result) {
        this.result = result;
        return this;
    }
}
