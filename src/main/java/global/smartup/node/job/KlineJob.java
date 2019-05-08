package global.smartup.node.job;

import global.smartup.node.constant.PoConstant;
import global.smartup.node.service.KlineNodeService;
import global.smartup.node.service.MarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KlineJob {

    private static final Logger log = LoggerFactory.getLogger(KlineJob.class);

    @Autowired
    private KlineNodeService klineNodeService;

    @Autowired
    private MarketService marketService;

    @Scheduled(cron = "1 0 * * * ?")
    public void keepNodeContinue() {
        List<String> markets = marketService.queryBuiltAndHasTrade();
        for (String market : markets) {
            for (String segment : PoConstant.KLineNode.Segment.All) {
                klineNodeService.keepNodeContinue(market, segment);
            }
        }
    }

}
