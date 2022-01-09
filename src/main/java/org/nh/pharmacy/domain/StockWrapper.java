package org.nh.pharmacy.domain;

import java.io.Serializable;

public class StockWrapper implements Serializable {
    private StockSource stockSource;
    private Stock stock;

    public StockSource getStockSource() {
        return stockSource;
    }

    public void setStockSource(StockSource stockSource) {
        this.stockSource = stockSource;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}
