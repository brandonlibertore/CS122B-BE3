package com.github.klefstad_teaching.cs122b.billing.repo;

import com.github.klefstad_teaching.cs122b.billing.data.Cart;
import com.github.klefstad_teaching.cs122b.billing.data.MovieInfo;
import com.github.klefstad_teaching.cs122b.billing.data.MovieObjects;
import com.github.klefstad_teaching.cs122b.billing.data.Sales;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class BillingRepo
{

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public BillingRepo(NamedParameterJdbcTemplate template)
    {
        this.template = template;
    }

    public void insertCart(Long user_id, Long movie_id, Integer quantity){
        // Insert into the billing.cart table the user id, movie id, and quantity.
        try{
            int rowsupdated = this.template.update("INSERT INTO billing.cart (user_id, movie_id, quantity)" +
                            "VALUES (:user_id, :movie_id, :quantity)",
                    new MapSqlParameterSource()
                            .addValue("user_id", user_id, Types.INTEGER)
                            .addValue("movie_id", movie_id, Types.INTEGER)
                            .addValue("quantity", quantity, Types.INTEGER));
        }
        catch (DuplicateKeyException e){
            throw new ResultError(BillingResults.CART_ITEM_EXISTS);
        }
    }

    public void updateCart(Long user_id, Long movie_id, Integer quantity){
        int rowsupdated = this.template.update("UPDATE billing.cart c SET c.quantity = :quantity WHERE c.movie_id = :movie_id",
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER)
                        .addValue("movie_id", movie_id, Types.INTEGER)
                        .addValue("quantity", quantity, Types.INTEGER));
        if (rowsupdated == 0){
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }
    }

    public void deleteCart(Long user_id, Long movie_id){
        int rowsupdated = this.template.update("DELETE FROM billing.cart c WHERE c.movie_id = :movie_id AND c.user_id = :user_id",
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER)
                        .addValue("movie_id", movie_id, Types.INTEGER));
        if (rowsupdated == 0){
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }
    }

    public List<MovieObjects> retrieveCart(Long user_id, List<String> userRole){
        List<MovieObjects> items = new ArrayList<>();
        if (userRole.contains("PREMIUM")){
            items = this.template.query(
                    "SELECT c.user_id, c.movie_id, c.quantity,  mp.movie_id, mp.unit_price, mp.premium_discount, m.title, m.backdrop_path, m.poster_path FROM billing.cart c JOIN billing.movie_price mp ON mp.movie_id = c.movie_id JOIN movies.movie m ON m.id = c.movie_id WHERE c.user_id = :user_id;",
                    new MapSqlParameterSource()
                            .addValue("user_id", user_id, Types.INTEGER)
                    , (rs, rowNum) ->
                            new MovieObjects()
                                    .setUnitprice(rs.getBigDecimal("unit_price").multiply(BigDecimal.valueOf(1 - (rs.getInt("premium_discount")/100.0))).setScale(2, RoundingMode.DOWN))
                                    .setQuantity((rs.getInt("quantity")))
                                    .setMovieId(rs.getLong("movie_id"))
                                    .setMovieTitle(rs.getString("title"))
                                    .setBackdropPath(rs.getString("backdrop_path"))
                                    .setPosterPath(rs.getString("poster_path")));
        }
        else{
            items = this.template.query(
                    "SELECT c.user_id, c.movie_id, c.quantity,  mp.movie_id, mp.unit_price, mp.premium_discount, m.title, m.backdrop_path, m.poster_path FROM billing.cart c JOIN billing.movie_price mp ON mp.movie_id = c.movie_id JOIN movies.movie m ON m.id = c.movie_id WHERE c.user_id = :user_id;",
                    new MapSqlParameterSource()
                            .addValue("user_id", user_id, Types.INTEGER)
                    , (rs, rowNum) ->
                            new MovieObjects()
                                    .setUnitprice(rs.getBigDecimal("unit_price").setScale(2, RoundingMode.DOWN))
                                    .setQuantity((rs.getInt("quantity")))
                                    .setMovieId(rs.getLong("movie_id"))
                                    .setMovieTitle(rs.getString("title"))
                                    .setBackdropPath(rs.getString("backdrop_path"))
                                    .setPosterPath(rs.getString("poster_path")));
        }
        return items;
    }

    public void clearCart(Long user_id){
        int rowsupdated = this.template.update("DELETE FROM billing.cart c WHERE c.user_id = :user_id",
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER));
        if (rowsupdated == 0){
            throw new ResultError(BillingResults.CART_EMPTY);
        }
    }

    public void addSale(Long user_id, BigDecimal total){
        int rowsupdated = this.template.update("INSERT INTO billing.sale (user_id, total, order_date)" +
                        "VALUES (:user_id, :total, :order_date)",
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER)
                        .addValue("total", total, Types.DECIMAL)
                        .addValue("order_date", Timestamp.from(Instant.now()), Types.TIMESTAMP));
    }

    public void addSaleItem(Long user_id){
        int rowsupdated = this.template.update(
                "INSERT INTO billing.sale_item (sale_id, movie_id, quantity) " +
                        "SELECT s.id, c.movie_id, c.quantity FROM billing.sale s  JOIN billing.cart c ON s.user_id = c.user_id " +
                        "WHERE c.user_id = :user_id",
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER));
    }

    public List<Sales> orderList(Long user_id){
        List<Sales> sales = this.template.query(
                "SELECT s.id, s.total, s.order_date FROM billing.sale s WHERE s.user_id = :user_id ORDER BY s.order_date DESC LIMIT 5",
                new MapSqlParameterSource()
                        .addValue("user_id", user_id, Types.INTEGER)
                , (rs, rowNum) ->
                        new Sales()
                                .setSaleId(rs.getLong("id"))
                                .setTotal(rs.getBigDecimal("total"))
                                .setOrderDate(rs.getTimestamp("order_date").toInstant()));
        return sales;
    }

    public List<MovieObjects> retrieveSale(Long user_id, Long sale_id, List<String> userRole){
        List<MovieObjects> items = new ArrayList<>();
        if (userRole.contains("PREMIUM")){
            items = this.template.query(
                    "SELECT DISTINCT c.user_id, c.movie_id, s.quantity,  mp.movie_id, mp.unit_price, mp.premium_discount, m.title, m.backdrop_path, m.poster_path " +
                            "FROM billing.cart c " +
                            "JOIN billing.movie_price mp ON mp.movie_id = c.movie_id " +
                            "JOIN movies.movie m ON m.id = c.movie_id " +
                            "JOIN billing.sale_item s ON s.movie_id = c.movie_id " +
                            "WHERE s.sale_id = :sale_id",
                    new MapSqlParameterSource()
                            .addValue("sale_id", sale_id, Types.INTEGER)
                    , (rs, rowNum) ->
                            new MovieObjects()
                                    .setUnitprice(rs.getBigDecimal("unit_price").multiply(BigDecimal.valueOf(1 - (rs.getInt("premium_discount")/100.0))).setScale(2, RoundingMode.DOWN))
                                    .setQuantity((rs.getInt("quantity")))
                                    .setMovieId(rs.getLong("movie_id"))
                                    .setMovieTitle(rs.getString("title"))
                                    .setBackdropPath(rs.getString("backdrop_path"))
                                    .setPosterPath(rs.getString("poster_path")));
        }
        else {
            items = this.template.query(
                    "SELECT DISTINCT c.user_id, c.movie_id, s.quantity,  mp.movie_id, mp.unit_price, mp.premium_discount, m.title, m.backdrop_path, m.poster_path " +
                            "FROM billing.cart c " +
                            "JOIN billing.movie_price mp ON mp.movie_id = c.movie_id " +
                            "JOIN movies.movie m ON m.id = c.movie_id " +
                            "JOIN billing.sale_item s ON s.movie_id = c.movie_id " +
                            "JOIN billing.sale ss ON ss.user_id = c.user_id " +
                            "WHERE s.sale_id = :sale_id AND ss.user_id = :user_id",
                    new MapSqlParameterSource()
                            .addValue("sale_id", sale_id, Types.INTEGER)
                            .addValue("user_id", user_id, Types.INTEGER)
                    , (rs, rowNum) ->
                            new MovieObjects()
                                    .setUnitprice(rs.getBigDecimal("unit_price").setScale(2, RoundingMode.DOWN))
                                    .setQuantity((rs.getInt("quantity")))
                                    .setMovieId(rs.getLong("movie_id"))
                                    .setMovieTitle(rs.getString("title"))
                                    .setBackdropPath(rs.getString("backdrop_path"))
                                    .setPosterPath(rs.getString("poster_path")));
        }
        return items;
    }
}
