package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "make_plan")
public class MakePlan {

    @Id
    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "sell_price")
    private BigDecimal sellPrice;

    @Column(name = "timestamp")
    private Long timestamp;

    @Column(name = "sign")
    private String sign;

    @Column(name = "create_time")
    private Date createTime;


    public Long getTimestamp() {
        return timestamp;
    }

    public MakePlan setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getTradeId() {
        return tradeId;
    }

    public MakePlan setTradeId(String tradeId) {
        this.tradeId = tradeId;
        return this;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public MakePlan setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public MakePlan setSign(String sign) {
        this.sign = sign;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public MakePlan setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }
}
