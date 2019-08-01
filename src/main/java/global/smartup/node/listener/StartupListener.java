package global.smartup.node.listener;

import global.smartup.node.Config;
import global.smartup.node.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class StartupListener implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(StartupListener.class);

    private ConfigurableApplicationContext context;

    @Autowired
    private Config config;

    @Autowired
    private DictService dictService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String publicKey = dictService.query("admin_public_key");
        String privateKey = dictService.query("admin_private_key");
        String blockNumber = dictService.query("block_number");
        if (StringUtils.isAnyBlank(publicKey, privateKey, blockNumber)) {
            log.error("---------------------------------------------------------------------");
            log.error("|You must set some config in your db table 'dict'                     |");
            log.error("|      admin_public_key='xx'                                          |");
            log.error("|      admin_private_key='xx'                                         |");
            log.error("|      block_number='0'                                               |");
            log.error("|Application will be closed                                           |");
            log.error("---------------------------------------------------------------------");
            context.close();
        } else {
            config.ethAdminPublicKey = publicKey;
            config.ethAdminPrivateKey = privateKey;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = (ConfigurableApplicationContext) applicationContext;
    }
}
