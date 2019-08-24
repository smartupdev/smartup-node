package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "trade_child_map")
public class TradeChildMap {

    @Id
    @Column(name = "trade_id")
    private String tradeId;

    @Id
    @Column(name = "child_id")
    private String childId;


    public String getTradeId() {
        return tradeId;
    }

    public TradeChildMap setTradeId(String tradeId) {
        this.tradeId = tradeId;
        return this;
    }

    public String getChildId() {
        return childId;
    }

    public TradeChildMap setChildId(String childId) {
        this.childId = childId;
        return this;
    }
}
