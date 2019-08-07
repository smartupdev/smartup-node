package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "trade_child")
public class TradeChild {

    @Id
    @Column(name = "tx_hash")
    private String txHash;

    @Column(name="trade_id")
    private String tradeId;

    @Column(name="volume")
    private BigDecimal volume;

    @Column(name="price")
    private BigDecimal price;

    @Column(name="create_time")
    private Date createTime;


    @Transient
    private Transaction transaction;



    public Transaction getTransaction() {
        return transaction;
    }

    public TradeChild setTransaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public String getTradeId() {
        return tradeId;
    }

    public TradeChild setTradeId(String tradeId) {
        this.tradeId = tradeId;
        return this;
    }

    public String getTxHash() {
        return txHash;
    }

    public TradeChild setTxHash(String txHash) {
        this.txHash = txHash;
        return this;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public TradeChild setVolume(BigDecimal volume) {
        this.volume = volume;
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public TradeChild setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public TradeChild setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }
}
