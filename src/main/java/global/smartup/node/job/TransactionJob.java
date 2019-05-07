package global.smartup.node.job;

import global.smartup.node.service.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransactionJob {

    @Autowired
    private BlockService blockService;

    @Scheduled(fixedDelay = 1000)
    public void blockListen () {
        blockService.handlePendingTransaction();
    }

}
