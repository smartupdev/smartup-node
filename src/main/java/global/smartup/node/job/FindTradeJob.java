package global.smartup.node.job;

import global.smartup.node.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FindTradeJob {

    @Autowired
    private TradeService tradeService;

    @Scheduled(fixedDelay = 1 * 1000)
    public void findTradeJob() {
        // 查询交易的具体数据
        tradeService.findTrade();
    }

}
