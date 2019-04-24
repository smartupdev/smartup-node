package global.smartup.node.service;


import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.po.Market;
import global.smartup.node.util.Pagination;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MarketServiceTest {

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private MarketService marketService;

    @Test
    public void add() {
        Market market = new Market();
    }

    @Test
    public void queryPage() {
        Pagination page = marketService.queryPage("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E", "lately_change", false, null, null);
        System.out.println(JSON.toJSONString(page));
    }

    @Test
    public void querySearchPage() {
        Pagination page = marketService.querySearchPage("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E", "z", "lately_change", false, null, null);
        System.out.println(JSON.toJSONString(page));
    }

    @Test
    public void queryTop() {
        List list = marketService.queryTop(null, PoConstant.Market.TopType.Newest, 20);
        System.out.println(JSON.toJSONString(list));
    }

}
