package global.smartup.node.match.bo;

import java.math.BigDecimal;

public class OrderChild {

    public String buyOrderId;

    private String sellOrderId;

    private BigDecimal price;

    private BigDecimal volume;

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public OrderChild setBuyOrderId(String buyOrderId) {
        this.buyOrderId = buyOrderId;
        return this;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public OrderChild setSellOrderId(String sellOrderId) {
        this.sellOrderId = sellOrderId;
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OrderChild setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public OrderChild setVolume(BigDecimal volume) {
        this.volume = volume;
        return this;
    }
}
