package global.smartup.node.fix;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.Starter;
import global.smartup.node.mapper.MarketMapper;
import global.smartup.node.mapper.NotificationMapper;
import global.smartup.node.po.Market;
import global.smartup.node.po.Notification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FixNotificationMarketName {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private MarketMapper marketMapper;

    private static List<Market> marketList = null;

    @Test
    public void fix() {
        marketList = marketMapper.selectAll();
        Integer pageNumb = 0;
        Page<Notification> page = null;
        do {
            pageNumb += 1;
            page = PageHelper.startPage(pageNumb, 200);
            notificationMapper.selectAll();

            for (Notification n : page.getResult()) {
                HashMap map = JSON.parseObject(n.getContent(), HashMap.class, Feature.UseBigDecimal);
                String marketId = (String) map.get("marketId");
                String marketName = queryMarketName(marketId);
                if (StringUtils.isNotBlank(marketName)) {
                    map.put("marketName", marketName);
                    n.setContent(JSON.toJSONString(map));
                    notificationMapper.updateByPrimaryKey(n);
                }
            }
        } while (page.getPageNum() < page.getPages());
    }

    private String queryMarketName(String marketId) {
        if (marketList != null && StringUtils.isNotBlank(marketId)) {
            for (Market market : marketList) {
                if (marketId.equals(market.getMarketId())) {
                    return market.getName();
                }
            }
        }
        return "";
    }

    @Test
    public void queryMarketNames() {
        marketList = marketMapper.selectAll();
        for (Market market : marketList) {
            System.out.println(market.getName());
        }
    }

    @Test
    public void queryNotificationMarketName() {
        Integer count = 0;
        Page<Notification> page = null;
        Integer pageNumb = 0;
        do {
            pageNumb += 1;
            page = PageHelper.startPage(pageNumb, 200);
            notificationMapper.selectAll();
            for (Notification n : page.getResult()) {
                count++;
                HashMap map = JSON.parseObject(n.getContent(), HashMap.class, Feature.UseBigDecimal);
                String marketName = (String) map.get("marketName");
                if (StringUtils.isBlank(marketName)) {
                    System.out.println(" ================================== " + count);
                }
            }
        } while (page.getPageNum() < page.getPages());
    }

}
