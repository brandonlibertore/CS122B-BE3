package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.github.klefstad_teaching.cs122b.billing.data.Sales;
import com.github.klefstad_teaching.cs122b.core.result.Result;

import java.util.List;

public class OrderListResponseModel {

    private Result result;
    private List<Sales> sales;

    public Result getResult() {
        return result;
    }

    public OrderListResponseModel setResult(Result result) {
        this.result = result;
        return this;
    }

    public List<Sales> getSales() {
        return sales;
    }

    public OrderListResponseModel setSales(List<Sales> sales) {
        this.sales = sales;
        return this;
    }
}
