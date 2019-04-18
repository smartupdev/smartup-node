package global.smartup.node.vo;

import java.math.BigDecimal;
import java.util.Date;

public class CTAccountWithMarket {

    private String marketId;

    private String marketAddress;

    private String marketCover;

    private BigDecimal latelyChange;


    private String userAddress;

    private BigDecimal ctAmount;

    private Date lastUpdateTime;


    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public String getMarketCover() {
        return marketCover;
    }

    public void setMarketCover(String marketCover) {
        this.marketCover = marketCover;
    }

    public BigDecimal getLatelyChange() {
        return latelyChange;
    }

    public void setLatelyChange(BigDecimal latelyChange) {
        this.latelyChange = latelyChange;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public BigDecimal getCtAmount() {
        return ctAmount;
    }

    public void setCtAmount(BigDecimal ctAmount) {
        this.ctAmount = ctAmount;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
