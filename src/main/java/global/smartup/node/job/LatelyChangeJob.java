package global.smartup.node.job;

import global.smartup.node.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时更新市场的24小时价格变化
 */
@Component
public class LatelyChangeJob {

    @Autowired
    private MarketService marketService;

    @Scheduled(cron = "0 0 * * * ?")
    public void updateLatelyChange () {
        marketService.updateLatelyChange();
    }

}
