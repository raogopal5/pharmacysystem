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
 * A ReserveStock.
 */
@Entity
@Table(name = "stock_reserve")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ReserveStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Float quantity;

    @NotNull
    @Column(name = "reserved_date", nullable = false)
    private LocalDateTime reservedDate;

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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStockId() {
        return stockId;
    }

    public ReserveStock stockId(Long stockId) {
        this.stockId = stockId;
        return this;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Float getQuantity() {
        return quantity;
    }

    public ReserveStock quantity(Float quantity) {
        this.quantity = quantity;
        return this;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getReservedDate() {
        return reservedDate;
    }

    public ReserveStock reservedDate(LocalDateTime reservedDate) {
        this.reservedDate = reservedDate;
        return this;
    }

    public void setReservedDate(LocalDateTime reservedDate) {
        this.reservedDate = reservedDate;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public ReserveStock transactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public ReserveStock transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public ReserveStock transactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
        return this;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public Long getTransactionLineId() {
        return transactionLineId;
    }

    public void setTransactionLineId(Long transactionLineId) {
        this.transactionLineId = transactionLineId;
    }

    public ReserveStock transactionLineId(Long transactionLineId) {
        this.transactionLineId = transactionLineId;
        return this;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public ReserveStock transactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

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
        ReserveStock reserveStock = (ReserveStock) o;
        if (reserveStock.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, reserveStock.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ReserveStock{" +
            "id=" + id +
            ", stockId='" + stockId + "'" +
            ", quantity='" + quantity + "'" +
            ", reservedDate='" + reservedDate + "'" +
            ", transactionId='" + transactionId + "'" +
            ", transactionType='" + transactionType + "'" +
            ", transactionNo='" + transactionNo + "'" +
            ", transactionLineId='" + transactionLineId + "'" +
            ", transactionDate='" + transactionDate + "'" +
            ", userId='" + userId + "'" +
            '}';
    }
}
