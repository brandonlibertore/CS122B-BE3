package com.github.klefstad_teaching.cs122b.billing.data;

import java.math.BigDecimal;

public class MovieInfo {

    private Long movieId;
    private Integer discount;

    public Long getMovieId() {
        return movieId;
    }

    public MovieInfo setMovieId(Long movieId) {
        this.movieId = movieId;
        return this;
    }

    public Integer getDiscount() {
        return discount;
    }

    public MovieInfo setDiscount(Integer discount) {
        this.discount = discount;
        return this;
    }
}
