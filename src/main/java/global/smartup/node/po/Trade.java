package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Column(name="sut_amount")
    private String sutAmount;

    @Column(name="ct_amount")
    private String ctAmount;

    @Column(name="time")
    private String time;

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

    public String getSutAmount() {
        return sutAmount;
    }

    public void setSutAmount(String sutAmount) {
        this.sutAmount = sutAmount;
    }

    public String getCtAmount() {
        return ctAmount;
    }

    public void setCtAmount(String ctAmount) {
        this.ctAmount = ctAmount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
