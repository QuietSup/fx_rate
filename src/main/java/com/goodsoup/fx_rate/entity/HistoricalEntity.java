package com.goodsoup.fx_rate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
        name = "historical",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_historical_pair_id_date", columnNames = {"pair_id", "date"})
        },
        indexes = {
                @Index(name = "idx_historical_pair_id", columnList = "pair_id"),
                @Index(name = "idx_historical_date", columnList = "date")
        }
)
public class HistoricalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pair_id", nullable = false)
    private PairEntity pair;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "high", nullable = false, precision = 12, scale = 5)
    private BigDecimal high;

    @Column(name = "low", nullable = false, precision = 12, scale = 5)
    private BigDecimal low;

    @Column(name = "close", nullable = false, precision = 12, scale = 5)
    private BigDecimal close;

    @Column(name = "change_pips", precision = 10, scale = 2)
    private BigDecimal changePips;

    @Column(name = "change_pct", precision = 8, scale = 2)
    private BigDecimal changePct;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PairEntity getPair() {
        return pair;
    }

    public void setPair(PairEntity pair) {
        this.pair = pair;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getChangePips() {
        return changePips;
    }

    public void setChangePips(BigDecimal changePips) {
        this.changePips = changePips;
    }

    public BigDecimal getChangePct() {
        return changePct;
    }

    public void setChangePct(BigDecimal changePct) {
        this.changePct = changePct;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

