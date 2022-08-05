package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.data.MovieObjects;
import com.github.klefstad_teaching.cs122b.billing.data.Sales;
import com.github.klefstad_teaching.cs122b.billing.model.request.OrderCompleteRequestModel;
import com.github.klefstad_teaching.cs122b.billing.model.response.*;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@RestController
public class OrderController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public OrderController(BillingRepo repo,Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @GetMapping("/order/payment")
    public ResponseEntity<OrderPaymentResponseMode> orderPayment(@AuthenticationPrincipal SignedJWT user) throws ParseException, StripeException {
        // Grab userId and role:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        List<String> userRole = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);

        List<MovieObjects> items = repo.retrieveCart(userId, userRole);
        // If the cart is empty:
        if (items.size() < 1){
            throw new ResultError(BillingResults.CART_EMPTY);
        }

        // Else calculate the total value of the cart given if the user is premium or not:
        BigDecimal total = BigDecimal.valueOf(0);
        for (MovieObjects item : items) {
            total = total.add(item.getUnitprice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        String description = "";
        for (int i = 0; i < items.size(); i++){
            if (i != items.size() - 1){
                description = description.concat(items.get(i).getMovieTitle().concat(", "));
            }
            else{
                description = description.concat(items.get(i).getMovieTitle());
            }
        }

        Long amountInTotalCents = total.movePointRight(2).longValue();
        String userIdStr = Long.toString(userId);

        PaymentIntentCreateParams paymentIntentCreateParams =
                PaymentIntentCreateParams
                        .builder()
                        .setCurrency("USD") // This will always be the same for our project
                        .setDescription(description)
                        .setAmount(amountInTotalCents)
                        // We use MetaData to keep track of the user that should pay for the order
                        .putMetadata("userId", userIdStr)
                        .setAutomaticPaymentMethods(
                                // This will tell stripe to generate the payment methods automatically
                                // This will always be the same for our project
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentCreateParams);
        String paymentIntentId = paymentIntent.getId();
        String clientSecret = paymentIntent.getClientSecret();

        // Create our response:
        OrderPaymentResponseMode body = new OrderPaymentResponseMode()
                .setResult(BillingResults.ORDER_PAYMENT_INTENT_CREATED)
                .setPaymentIntentId(paymentIntentId)
                .setClientSecret(clientSecret);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }

    @PostMapping("/order/complete")
    public ResponseEntity<OrderCompleteResponseModel> orderComplete(@AuthenticationPrincipal SignedJWT user,
                                                                    @RequestBody OrderCompleteRequestModel request) throws ParseException, StripeException {
        // Grab userId and role:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        List<String> userRole = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);

        String paymentIntentId = request.getPaymentIntentId();
        PaymentIntent retrievedPaymentIntent = PaymentIntent.retrieve(paymentIntentId);
        String status = retrievedPaymentIntent.getStatus();
        Long retrievedId = Long.parseLong(retrievedPaymentIntent.getMetadata().get("userId"));
        BigDecimal amountInTotalCents = BigDecimal.valueOf(retrievedPaymentIntent.getAmount()).movePointLeft(2);
        if (!status.equals("succeeded")){
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_NOT_SUCCEEDED);
        }
        if (!Objects.equals(userId, retrievedId)){
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_WRONG_USER);
        }

        List<MovieObjects> items = repo.retrieveCart(userId, userRole);
        for (MovieObjects item : items){
            System.out.println(item.getMovieId());
        }

        repo.addSale(userId, amountInTotalCents);
        repo.addSaleItem(userId);

        // Clear cart of purchased items:
//        repo.clearCart(userId);

        // Create our response:
        OrderCompleteResponseModel body = new OrderCompleteResponseModel()
                .setResult(BillingResults.ORDER_COMPLETED);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }

    @GetMapping("/order/list")
    public ResponseEntity<OrderListResponseModel> orderList(@AuthenticationPrincipal SignedJWT user) throws ParseException, StripeException {
        // Grab userId:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        List<Sales> sales = repo.orderList(userId);
        if (sales.size() < 1){
            throw new ResultError(BillingResults.ORDER_LIST_NO_SALES_FOUND);
        }

        // Create our response:
        OrderListResponseModel body = new OrderListResponseModel()
                .setResult(BillingResults.ORDER_LIST_FOUND_SALES)
                .setSales(sales);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }

    @GetMapping("/order/detail/{saleId}")
    public ResponseEntity<OrderDetailResponseModel> orderDetail(@AuthenticationPrincipal SignedJWT user,
                                                @PathVariable Long saleId) throws ParseException, StripeException {
        // Grab userId and role:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        List<String> userRole = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);

        // Grab list of items in a users cart:
        List<MovieObjects> items = repo.retrieveSale(userId, saleId, userRole);
        for (MovieObjects item : items){
            System.out.println(userId + " " + item.getQuantity() + " " + item.getMovieId() + " " + item.getUnitprice());
        }

        // If the cart is empty:
        if (items.size() < 1){
            throw new ResultError(BillingResults.ORDER_DETAIL_NOT_FOUND);
        }

        // Seen movieid:
        List<Long> seenId = new ArrayList<>();

        // Updated list to remove duplicates:
        List<MovieObjects> updatedItems = new ArrayList<>();
        for (MovieObjects item: items){
            if (!seenId.contains(item.getMovieId())){
                seenId.add(item.getMovieId());
                updatedItems.add(item);
            }
        }

        // Else calculate the total value of the cart given if the user is premium or not:
        BigDecimal total = BigDecimal.valueOf(0);
        for (MovieObjects item : updatedItems) {
            total = total.add(item.getUnitprice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Create our response:
        OrderDetailResponseModel body = new OrderDetailResponseModel()
                .setResult(BillingResults.ORDER_DETAIL_FOUND)
                .setTotal(total)
                .setItems(updatedItems);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }
}
