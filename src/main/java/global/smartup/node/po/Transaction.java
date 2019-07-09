package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

public class Transaction {

    @Id
    @Column(name = "tx_hash")
    private String txHash;

    /**
     * {@link global.smartup.node.constant.PoConstant.TxStage}
     */
    @Column(name = "stage")
    private String stage;

    /**
     * {@link global.smartup.node.constant.PoConstant.Transaction.Type}
     */
    @Column(name = "type")
    private String type;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "detail")
    private String detail;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "block_time")
    private Date blockTime;



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
