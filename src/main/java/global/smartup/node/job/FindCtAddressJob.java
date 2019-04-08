package global.smartup.node.job;

import global.smartup.node.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FindCtAddressJob {

    @Autowired
    private MarketService marketService;

    @Scheduled(fixedDelay = 1 * 1000)
    public void findCtAddress() {
        marketService.updateCreatingToBuilt();
    }

}
