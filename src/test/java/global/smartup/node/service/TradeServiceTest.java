package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.util.Pagination;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradeServiceTest {


    @Autowired
    private TradeService tradeService;

    @Test
    public void queryByMarket() {
        Pagination page = tradeService.queryByMarket("0xcc5460904f28bf33fe88d713295d13f4c187b46b",
                // PoConstant.Trade.Type.Sell,
                null,
                true, 1, 10);
        System.out.println(JSON.toJSONString(page));

    }

}

