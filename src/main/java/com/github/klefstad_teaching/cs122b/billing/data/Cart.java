package com.github.klefstad_teaching.cs122b.billing.data;

public class Cart {
    private Integer user_id;
    private Long movie_id;
    private Integer quantity;

    public Integer getUser_id() {
        return user_id;
    }

    public Cart setUser_id(Integer user_id) {
        this.user_id = user_id;
        return this;
    }

    public Long getMovie_id() {
        return movie_id;
    }

    public Cart setMovie_id(Long movie_id) {
        this.movie_id = movie_id;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Cart setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }
}
