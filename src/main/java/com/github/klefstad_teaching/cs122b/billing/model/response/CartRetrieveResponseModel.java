package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.billing.data.MovieObjects;
import com.github.klefstad_teaching.cs122b.core.result.Result;

import java.math.BigDecimal;
import java.util.List;

public class CartRetrieveResponseModel {

    private Result result;
    private BigDecimal total;
    private List<MovieObjects> items;

    public Result getResult() {
        return result;
    }

    public CartRetrieveResponseModel setResult(Result result) {
        this.result = result;
        return this;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public CartRetrieveResponseModel setTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public List<MovieObjects> getItems() {
        return items;
    }

    public CartRetrieveResponseModel setItems(List<MovieObjects> items) {
        this.items = items;
        return this;
    }
}
