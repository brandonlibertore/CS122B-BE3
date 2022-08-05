package com.github.klefstad_teaching.cs122b.billing.util;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import org.springframework.stereotype.Component;

@Component
public final class Validate
{
    public void validityCheck(Integer quantity){
        // Check if quantity is within the minimum range:
        if (quantity < 1){
            throw new ResultError(BillingResults.INVALID_QUANTITY);
        }
        // Check if quantity is within the maximum range:
        if (quantity > 10){
            throw new ResultError(BillingResults.MAX_QUANTITY);
        }
    }
}
