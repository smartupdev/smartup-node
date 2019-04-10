package global.smartup.node.vo;

import java.math.BigDecimal;

public class MarketData {

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal lastPrice;

    private BigDecimal todayVolume;

    private BigDecimal sutPool;

    private BigDecimal allSellCT;


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

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public BigDecimal getTodayVolume() {
        return todayVolume;
    }

    public void setTodayVolume(BigDecimal todayVolume) {
        this.todayVolume = todayVolume;
    }

    public BigDecimal getSutPool() {
        return sutPool;
    }

    public void setSutPool(BigDecimal sutPool) {
        this.sutPool = sutPool;
    }

    public BigDecimal getAllSellCT() {
        return allSellCT;
    }

    public void setAllSellCT(BigDecimal allSellCT) {
        this.allSellCT = allSellCT;
    }
}
