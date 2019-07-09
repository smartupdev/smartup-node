package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.Starter;
import global.smartup.node.mapper.NotificationMapper;
import global.smartup.node.po.Notification;
import global.smartup.node.util.Pagination;
import global.smartup.node.vo.Ntfc;
import global.smartup.node.vo.UnreadNtfc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Test
    public void sendTradeFinish() {
        notificationService.sendTradeFinish("tx", true, "user", "buy", "marketId", "market", "",
                new BigDecimal("0.00000000000000000000"), BigDecimal.ZERO);
    }

    @Test
    public void queryUnreadInCache() {
        UnreadNtfc ntfc = notificationService.queryUnreadInCache("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E", Locale.CHINA);
        System.out.println(JSON.toJSONString(ntfc));
    }

    @Test
    public void querySearch() {
        Pagination<Ntfc> page = notificationService.querySearch("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E", "創建", 1, 10, Locale.TRADITIONAL_CHINESE);
        System.out.println(JSON.toJSONString(page));
    }

}
