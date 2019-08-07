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


    public String getTxHash() {
        return txHash;
    }

    public Transaction setTxHash(String txHash) {
        this.txHash = txHash;
        return this;
    }

    public String getStage() {
        return stage;
    }

    public Transaction setStage(String stage) {
        this.stage = stage;
        return this;
    }

    public String getType() {
        return type;
    }

    public Transaction setType(String type) {
        this.type = type;
        return this;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public Transaction setUserAddress(String userAddress) {
        this.userAddress = userAddress;
        return this;
    }

    public String getDetail() {
        return detail;
    }

    public Transaction setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Transaction setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getBlockTime() {
        return blockTime;
    }

    public Transaction setBlockTime(Date blockTime) {
        this.blockTime = blockTime;
        return this;
    }
}
