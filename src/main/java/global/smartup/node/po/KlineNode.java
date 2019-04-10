package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * redis key :
 * [market]_hour_2019_04_01
 * [market]_day_2019_04
 * [market]_week_2019
 */
@Table(name = "kline_node")
public class KlineNode {

    @Id
    @Column(name = "market_address")
    private String marketAddress;

    @Id
    @Column(name = "time_id")
    private String timeId;

    /**
     * {@link global.smartup.node.constant.PoConstant.KLineNode.Segment}
     */
    @Id
    @Column(name = "segment")
    private String segment;

    @Column(name = "high")
    private BigDecimal high;

    @Column(name = "low")
    private BigDecimal low;

    @Column(name = "start")
    private BigDecimal start;

    @Column(name = "end")
    private BigDecimal end;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "count")
    private Long count;

    @Column(name = "time")
    private Date time;



    public String getTimeId() {
        return timeId;
    }

    public void setTimeId(String timeId) {
        this.timeId = timeId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getStart() {
        return start;
    }

    public void setStart(BigDecimal start) {
        this.start = start;
    }

    public BigDecimal getEnd() {
        return end;
    }

    public void setEnd(BigDecimal end) {
        this.end = end;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
