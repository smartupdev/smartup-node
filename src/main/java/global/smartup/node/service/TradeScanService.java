package global.smartup.node.service;

import global.smartup.node.mapper.TakePlanMapper;
import global.smartup.node.mapper.TradeChildMapper;
import global.smartup.node.po.TakePlan;
import global.smartup.node.po.Trade;
import global.smartup.node.po.TradeChild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeScanService {


    @Autowired
    private TakePlanMapper takePlanMapper;

    @Autowired
    private TradeChildMapper tradeChildMapper;

    public TakePlan queryTopTakePlan() {
        return takePlanMapper.queryTop();
    }

    public List<TradeChild> queryChild(String takePlanId) {
        TradeChild condition = new TradeChild();
        condition.setTakePlanId(takePlanId);
        return tradeChildMapper.select(condition);
    }

}
