package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nh.pharmacy.domain.enumeration.TransactionType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A StockTransit.
 */
@Entity
@Table(name = "stock_transit")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StockTransit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Float quantity;

    @NotNull
    @Column(name = "transit_date", nullable = false)
    private LocalDateTime transitDate;

    @NotNull
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @NotNull
    @Column(name = "transaction_no", nullable = false)
    private String transactionNo;

    @NotNull
    @Column(name = "transaction_line_id", nullable = false)
    private Long transactionLineId;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @NotNull
    @Column(name = "pending_quantity", nullable = false)
    private Float pendingQuantity;

    @Transient
    private Long userId;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStockId() {
        return stockId;
    }

    public StockTransit stockId(Long stockId) {
        this.stockId = stockId;
        return this;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Float getQuantity() {
        return quantity;
    }

    public StockTransit quantity(Float quantity) {
        this.quantity = quantity;
        return this;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTransitDate() {
        return transitDate;
    }

    public StockTransit transitDate(LocalDateTime transitDate) {
        this.transitDate = transitDate;
        return this;
    }

    public void setTransitDate(LocalDateTime transitDate) {
        this.transitDate = transitDate;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public StockTransit transactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public StockTransit transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public StockTransit transactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
        return this;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public Long getTransactionLineId() {
        return transactionLineId;
    }

    public StockTransit transactionLineId(Long transactionLineId) {
        this.transactionLineId = transactionLineId;
        return this;
    }

    public void setTransactionLineId(Long transactionLineId) {
        this.transactionLineId = transactionLineId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public StockTransit transactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }


    public Float getPendingQuantity() {
        return pendingQuantity;
    }

    public StockTransit pendingQuantity(Float pendingQuantity) {
        this.pendingQuantity = pendingQuantity;
        return this;
    }

    public void setPendingQuantity(Float pendingQuantity) {
        this.pendingQuantity = pendingQuantity;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StockTransit stockTransit = (StockTransit) o;
        if (stockTransit.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), stockTransit.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "StockTransit{" +
            "id=" + getId() +
            ", stockId='" + getStockId() + "'" +
            ", quantity='" + getQuantity() + "'" +
            ", transitDate='" + getTransitDate() + "'" +
            ", transactionId='" + getTransactionId() + "'" +
            ", transactionType='" + getTransactionType() + "'" +
            ", transactionNo='" + getTransactionNo() + "'" +
            ", transactionLineId='" + getTransactionLineId() + "'" +
            ", transactionDate='" + getTransactionDate() + "'" +
            ", pendingQuantity='" + getPendingQuantity() + "'" +
            "}";
    }
}
