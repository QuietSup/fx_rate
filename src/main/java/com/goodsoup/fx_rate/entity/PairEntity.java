package com.goodsoup.fx_rate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "pairs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_pairs_base_quote", columnNames = {"base", "quote"})
        },
        indexes = {
                @Index(name = "idx_pairs_base_quote", columnList = "base, quote")
        }
)
public class PairEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base", nullable = false, length = 10)
    private String base;

    @Column(name = "quote", nullable = false, length = 10)
    private String quote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }
}

