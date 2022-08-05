package com.github.klefstad_teaching.cs122b.billing.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class MovieObjects {

    private BigDecimal unitprice;
    private Integer quantity;
    private Long movieId;
    private String movieTitle;
    private String backdropPath;
    private String posterPath;

    @JsonProperty("unitPrice")
    public BigDecimal getUnitprice() {
        return unitprice;
    }

    public MovieObjects setUnitprice(BigDecimal unitprice) {
        this.unitprice = unitprice;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public MovieObjects setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public Long getMovieId() {
        return movieId;
    }

    public MovieObjects setMovieId(Long movieId) {
        this.movieId = movieId;
        return this;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public MovieObjects setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
        return this;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public MovieObjects setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
        return this;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public MovieObjects setPosterPath(String posterPath) {
        this.posterPath = posterPath;
        return this;
    }
}
