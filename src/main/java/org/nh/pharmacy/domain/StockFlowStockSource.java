package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "stock_flow_stock_source")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StockFlowStockSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "stock_flow_id", nullable = false)
    private Long stockFlowId;

    @NotNull
    @Column(name = "stock_source_id", nullable = false)
    private Long stockSourceId;

    public StockFlowStockSource() {
    }

    public StockFlowStockSource(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public StockFlowStockSource stockFlowId(Long stockFlowId) {
        this.stockFlowId = stockFlowId;
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStockFlowId() {
        return stockFlowId;
    }

    public void setStockFlowId(Long stockFlowId) {
        this.stockFlowId = stockFlowId;
    }

    public Long getStockSourceId() {
        return stockSourceId;
    }

    public StockFlowStockSource stockSourceId(Long stockSourceId) {
        this.stockSourceId = stockSourceId;
        return this;
    }

    public void setStockSourceId(Long stockSourceId) {
        this.stockSourceId = stockSourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StockFlowStockSource that = (StockFlowStockSource) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (stockFlowId != null ? !stockFlowId.equals(that.stockFlowId) : that.stockFlowId != null) return false;
        return stockSourceId != null ? stockSourceId.equals(that.stockSourceId) : that.stockSourceId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (stockFlowId != null ? stockFlowId.hashCode() : 0);
        result = 31 * result + (stockSourceId != null ? stockSourceId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StockFlowStockSource{" +
            "id=" + id +
            ", stockFlowId=" + stockFlowId +
            ", stockSourceId=" + stockSourceId +
            '}';
    }
}
