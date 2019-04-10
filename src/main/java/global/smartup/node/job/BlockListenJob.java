package global.smartup.node.job;

import global.smartup.node.eth.BlockListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BlockListenJob {

    @Autowired
    private BlockListener blockListener;

    @Scheduled(fixedDelay = 500)
    public void blockListen () {
        blockListener.start();
    }

}
