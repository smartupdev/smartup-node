package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Table(name = "fund_flow")
public class FundFlow {

    @Id
    @Column(name = "flow_id")
    private String flowId;

    @Column(name = "user_address")
    private String userAddress;

    /**
     * {@link global.smartup.node.constant.PoConstant.Currency}
     */
    @Column(name = "currency")
    private String currency;

    /**
     * {@link global.smartup.node.constant.PoConstant.FundFlow.Type}
     */
    @Column(name = "type")
    private String type;

    /**
     * {@link global.smartup.node.constant.PoConstant.FundFlow.Direction}
     */
    @Column(name = "direction")
    private String direction;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "fee")
    private BigDecimal fee;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Column(name = "create_time")
    private Date createTime;



    @Transient
    private List<Transaction> txList;



    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean success) {
        isSuccess = success;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<Transaction> getTxList() {
        return txList;
    }

    public void setTxList(List<Transaction> txList) {
        this.txList = txList;
    }
}
