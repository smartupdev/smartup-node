package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Table(name = "trade")
public class Trade {

    @Id
    @Column(name="trade_id")
    private String tradeId;

    @Column(name="user_address")
    private String userAddress;

    @Column(name="market_Id")
    private String marketId;

    /**
     * {@link global.smartup.node.constant.PoConstant.Trade.Type}
     */
    @Column(name="type")
    private String type;

    /**
     * {@link global.smartup.node.constant.PoConstant.Trade.State}
     */
    @Column(name="state")
    private String state;

    @Column(name="entrust_volume")
    private BigDecimal entrustVolume;

    @Column(name="entrust_price")
    private BigDecimal entrustPrice;

    @Column(name="filled_volume")
    private BigDecimal filledVolume;

    @Column(name="avg_price")
    private BigDecimal avgPrice;

    @Column(name="fee")
    private BigDecimal fee;

    @Column(name="sign")
    private String sign;

    @Column(name="create_time")
    private Date createTime;

    @Column(name="update_time")
    private Date updateTime;


    @Transient
    private User user;

    @Transient
    private List<TradeChild> childList;


    public String getSign() {
        return sign;
    }

    public Trade setSign(String sign) {
        this.sign = sign;
        return this;
    }

    public String getTradeId() {
        return tradeId;
    }

    public Trade setTradeId(String tradeId) {
        this.tradeId = tradeId;
        return this;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public Trade setUserAddress(String userAddress) {
        this.userAddress = userAddress;
        return this;
    }

    public String getMarketId() {
        return marketId;
    }

    public Trade setMarketId(String marketId) {
        this.marketId = marketId;
        return this;
    }

    public String getType() {
        return type;
    }

    public Trade setType(String type) {
        this.type = type;
        return this;
    }

    public String getState() {
        return state;
    }

    public Trade setState(String state) {
        this.state = state;
        return this;
    }

    public BigDecimal getEntrustVolume() {
        return entrustVolume;
    }

    public Trade setEntrustVolume(BigDecimal entrustVolume) {
        this.entrustVolume = entrustVolume;
        return this;
    }

    public BigDecimal getEntrustPrice() {
        return entrustPrice;
    }

    public Trade setEntrustPrice(BigDecimal entrustPrice) {
        this.entrustPrice = entrustPrice;
        return this;
    }

    public BigDecimal getFilledVolume() {
        return filledVolume;
    }

    public Trade setFilledVolume(BigDecimal filledVolume) {
        this.filledVolume = filledVolume;
        return this;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public Trade setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
        return this;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public Trade setFee(BigDecimal fee) {
        this.fee = fee;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Trade setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Trade setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Trade setUser(User user) {
        this.user = user;
        return this;
    }

    public List<TradeChild> getChildList() {
        return childList;
    }

    public Trade setChildList(List<TradeChild> childList) {
        this.childList = childList;
        return this;
    }
}
