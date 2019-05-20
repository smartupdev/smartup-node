package global.smartup.node.job;

import global.smartup.node.service.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserAccountJob {

    private static final Logger log = LoggerFactory.getLogger(UserAccountJob.class);

    @Autowired
    private UserAccountService userAccountService;

    @Scheduled(fixedDelay = 1 * 60 * 1000)
    public void keepNodeContinue() {
        userAccountService.updateAllAccount();
    }

}
