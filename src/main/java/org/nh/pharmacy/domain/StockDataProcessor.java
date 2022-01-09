package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A StockDataProcessor.
 */
@Entity
@Table(name = "stock_data_processor")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StockDataProcessor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull
    @Column(name = "process_flag", nullable = false)
    private Boolean processFlag = false;

    public Long getId() {
        return id;
    }

    public StockDataProcessor() {
    }

    public StockDataProcessor(Long id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isProcessFlag() {
        return processFlag;
    }

    public StockDataProcessor processFlag(Boolean processFlag) {
        this.processFlag = processFlag;
        return this;
    }

    public void setProcessFlag(Boolean processFlag) {
        this.processFlag = processFlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StockDataProcessor stockDataProcessor = (StockDataProcessor) o;
        if (stockDataProcessor.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, stockDataProcessor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "StockDataProcessor{" +
            "id=" + id +
            ", processFlag='" + processFlag + "'" +
            '}';
    }
}
