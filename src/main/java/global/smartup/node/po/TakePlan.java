package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "take_plan")
public class TakePlan {

    @Column(name = "take_plan_id")
    private String takePlanId;

    @Column(name = "take_trade_id")
    private String takeTradeId;

    // 要匹配的子订单数量
    @Column(name = "child_size")
    private Integer childSize;

    // take的次数，可能会上浮10%，计算出gas limit
    @Column(name = "times")
    private Integer times;

    @Column(name = "gas_price")
    private Long gasPrice;

    @Column(name = "gas_limit")
    private Long gasLimit;

    @Column(name = "timestamp")
    private Long timestamp;

    @Column(name = "sign")
    private String sign;

    @Column(name = "is_over")
    private Boolean isOver;

    @Column(name = "create_time")
    private Date createTime;


    public Integer getChildSize() {
        return childSize;
    }

    public TakePlan setChildSize(Integer childSize) {
        this.childSize = childSize;
        return this;
    }

    public Long getGasPrice() {
        return gasPrice;
    }

    public TakePlan setGasPrice(Long gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    public Long getGasLimit() {
        return gasLimit;
    }

    public TakePlan setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public TakePlan setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getTakeTradeId() {
        return takeTradeId;
    }

    public TakePlan setTakeTradeId(String takeTradeId) {
        this.takeTradeId = takeTradeId;
        return this;
    }

    public String getTakePlanId() {
        return takePlanId;
    }

    public TakePlan setTakePlanId(String takePlanId) {
        this.takePlanId = takePlanId;
        return this;
    }

    public Integer getTimes() {
        return times;
    }

    public TakePlan setTimes(Integer times) {
        this.times = times;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public TakePlan setSign(String sign) {
        this.sign = sign;
        return this;
    }

    public Boolean getIsOver() {
        return isOver;
    }

    public TakePlan setIsOver(Boolean over) {
        isOver = over;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public TakePlan setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }
}
