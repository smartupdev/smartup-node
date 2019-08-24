package global.smartup.node.match.bo;

import global.smartup.node.match.common.OrderType;

import java.math.BigDecimal;

public class Order {

    private String orderId;

    private OrderType type;

    private String userAddress;

    private BigDecimal entrustPrice;

    private BigDecimal entrustVolume;

    // 未交易的量
    private BigDecimal unfilledVolume;

    // 剩余匹配次数
    private Integer times;


    public BigDecimal getUnfilledVolume() {
        return unfilledVolume;
    }

    public Order setUnfilledVolume(BigDecimal unfilledVolume) {
        this.unfilledVolume = unfilledVolume;
        return this;
    }

    public Integer getTimes() {
        return times;
    }

    public Order setTimes(Integer times) {
        this.times = times;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public Order setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public OrderType getType() {
        return type;
    }

    public Order setType(OrderType type) {
        this.type = type;
        return this;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public Order setUserAddress(String userAddress) {
        this.userAddress = userAddress;
        return this;
    }

    public BigDecimal getEntrustPrice() {
        return entrustPrice;
    }

    public Order setEntrustPrice(BigDecimal entrustPrice) {
        this.entrustPrice = entrustPrice;
        return this;
    }

    public BigDecimal getEntrustVolume() {
        return entrustVolume;
    }

    public Order setEntrustVolume(BigDecimal entrustVolume) {
        this.entrustVolume = entrustVolume;
        return this;
    }

}
