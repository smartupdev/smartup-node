package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name = "market_data")
public class MarketData {

    @Id
    @Column(name = "market_address")
    private String marketAddress;

    // 最近变化率
    @Column(name = "lately_change")
    private BigDecimal latelyChange;

    // 最后一笔价格
    @Column(name = "last")
    private BigDecimal last;

    // 最近交易量
    @Column(name = "lately_volume")
    private BigDecimal latelyVolume;

    // 市场中的SUT
    @Column(name = "amount")
    private BigDecimal amount;

    // 市场发行的CT
    @Column(name = "ct_amount")
    private BigDecimal ctAmount;

    // 市场最多发行的CT
    @Column(name = "ct_top_amount")
    private BigDecimal ctTopAmount;

    // 总交易次数
    @Column(name = "count")
    private Long count;

    // 帖子数量
    @Column(name = "post_count")
    private Integer postCount;

    // 用户数量
    @Column(name = "user_count")
    private Integer userCount;



    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public BigDecimal getCtAmount() {
        return ctAmount;
    }

    public void setCtAmount(BigDecimal ctAmount) {
        this.ctAmount = ctAmount;
    }

    public BigDecimal getCtTopAmount() {
        return ctTopAmount;
    }

    public void setCtTopAmount(BigDecimal ctTopAmount) {
        this.ctTopAmount = ctTopAmount;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public BigDecimal getLatelyChange() {
        return latelyChange;
    }

    public void setLatelyChange(BigDecimal latelyChange) {
        this.latelyChange = latelyChange;
    }

    public BigDecimal getLast() {
        return last;
    }

    public void setLast(BigDecimal last) {
        this.last = last;
    }

    public BigDecimal getLatelyVolume() {
        return latelyVolume;
    }

    public void setLatelyVolume(BigDecimal latelyVolume) {
        this.latelyVolume = latelyVolume;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
