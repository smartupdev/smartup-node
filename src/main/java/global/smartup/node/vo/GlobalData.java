package global.smartup.node.vo;

import java.math.BigDecimal;

public class GlobalData {

    private BigDecimal sutAmount;

    private Integer marketCount;

    private Integer latelyPostCount;


    public BigDecimal getSutAmount() {
        return sutAmount;
    }

    public void setSutAmount(BigDecimal sutAmount) {
        this.sutAmount = sutAmount;
    }

    public Integer getMarketCount() {
        return marketCount;
    }

    public void setMarketCount(Integer marketCount) {
        this.marketCount = marketCount;
    }

    public Integer getLatelyPostCount() {
        return latelyPostCount;
    }

    public void setLatelyPostCount(Integer latelyPostCount) {
        this.latelyPostCount = latelyPostCount;
    }
}
