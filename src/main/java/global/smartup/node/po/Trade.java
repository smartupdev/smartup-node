package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "trade")
public class Trade {

    @Id
    @Column(name="tx_hash")
    private String txHash;

    @Column(name="stage")
    private String stage;

    @Column(name="user_address")
    private String userAddress;

    @Column(name="market_address")
    private String marketAddress;

    @Column(name="type")
    private String type;

    @Column(name="sut_offer")
    private BigDecimal sutOffer;

    @Column(name="sut_amount")
    private BigDecimal sutAmount;

    @Column(name="ct_amount")
    private BigDecimal ctAmount;

    @Column(name="create_time")
    private Date createTime;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getSutOffer() {
        return sutOffer;
    }

    public void setSutOffer(BigDecimal sutOffer) {
        this.sutOffer = sutOffer;
    }

    public BigDecimal getSutAmount() {
        return sutAmount;
    }

    public void setSutAmount(BigDecimal sutAmount) {
        this.sutAmount = sutAmount;
    }

    public BigDecimal getCtAmount() {
        return ctAmount;
    }

    public void setCtAmount(BigDecimal ctAmount) {
        this.ctAmount = ctAmount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
