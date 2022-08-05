package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.data.MovieInfo;
import com.github.klefstad_teaching.cs122b.billing.data.MovieObjects;
import com.github.klefstad_teaching.cs122b.billing.model.response.*;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.model.request.CartInsertRequestModel;
import com.github.klefstad_teaching.cs122b.billing.model.request.CartUpdateRequestModel;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

@RestController
public class CartController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public CartController(BillingRepo repo, Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @PostMapping("/cart/insert")
    public ResponseEntity<CartInsertResponseModel> cartInsert(@AuthenticationPrincipal SignedJWT user,
                                                              @RequestBody CartInsertRequestModel request) throws ParseException {
        // Grab userId, movieId, and quantity:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        Long movieId = request.getMovieId();
        Integer quantity = request.getQuantity();

        // Check validity of arguments:
        validate.validityCheck(quantity);

        // If there are no invalid inputs attempt to insert, and if duplicate key exception is thrown
        // then that movie is already in the cart:
        repo.insertCart(userId, movieId, quantity);

        // Create our response:
        CartInsertResponseModel body = new CartInsertResponseModel()
                .setResult(BillingResults.CART_ITEM_INSERTED);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }

    @PostMapping("/cart/update")
    public ResponseEntity<CartUpdateResponseModel> cartUpdate(@AuthenticationPrincipal SignedJWT user,
                                                              @RequestBody CartUpdateRequestModel request) throws ParseException {
        // Grab userId, movieId, and quantity:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        Long movieId = request.getMovieId();
        Integer quantity = request.getQuantity();

        // Check validity of arguments:
        validate.validityCheck(quantity);

        // If there are no invalid inputs attempt to update, and if exception occurs movie is not in cart:
        repo.updateCart(userId, movieId, quantity);

        // Create our response:
        CartUpdateResponseModel body = new CartUpdateResponseModel()
                .setResult(BillingResults.CART_ITEM_UPDATED);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }

    @DeleteMapping("/cart/delete/{movieId}")
    public ResponseEntity<CartDeleteResponseModel> cartUpdate(@AuthenticationPrincipal SignedJWT user,
                                                              @PathVariable Long movieId) throws ParseException {
        // Grab userId:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        // Attempt to delete movie from cart:
        repo.deleteCart(userId, movieId);

        // Create our response:
        CartDeleteResponseModel body = new CartDeleteResponseModel()
                .setResult(BillingResults.CART_ITEM_DELETED);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }

    @GetMapping("/cart/retrieve")
    public ResponseEntity<CartRetrieveResponseModel> cartRetrieve(@AuthenticationPrincipal SignedJWT user) throws ParseException {

        // Grab userId and role:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        List<String> userRole = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);

        // Grab list of items in a users cart:
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

        // Create our response:
        CartRetrieveResponseModel body = new CartRetrieveResponseModel()
                .setResult(BillingResults.CART_RETRIEVED)
                .setTotal(total)
                .setItems(items);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }

    @PostMapping("/cart/clear")
    public ResponseEntity<CartClearResponseModel> cartClear(@AuthenticationPrincipal SignedJWT user) throws ParseException {
        // Grab userId and role:
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        List<String> userRole = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);

        repo.clearCart(userId);

        // Create our response:
        CartClearResponseModel body = new CartClearResponseModel()
                .setResult(BillingResults.CART_CLEARED);

        return ResponseEntity
                .status(body.getResult().status())
                .body(body);
    }
}
