package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

public class Transaction {

    @Id
    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "stage")
    private String stage;

    @Column(name = "type")
    private String type;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "market_id")
    private String marketId;

    @Column(name = "market_address")
    private String marketAddress;

    @Column(name = "detail")
    private String detail;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "block_time")
    private Date blockTime;


    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Date getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Date blockTime) {
        this.blockTime = blockTime;
    }
}
