package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.vo.UnreadNtfc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    public void sendTradeFinish() {
        notificationService.sendTradeFinish("tx", true, "user", "buy", "market",
                new BigDecimal("0.00000000000000000000"), BigDecimal.ZERO);
    }

    @Test
    public void queryUnreadInCache() {
        UnreadNtfc ntfc = notificationService.queryUnreadInCache("user");
        System.out.println(JSON.toJSONString(ntfc));
    }

}
