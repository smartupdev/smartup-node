package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.util.Pagination;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CTAccountServiceTest {

    @Autowired
    private CTAccountService ctAccountService;

    @Test
    public void update() {
        ctAccountService.update("0xm1", "0xu1", BigDecimal.TEN);
    }

    @Test
    public void queryWidthMarket() {
        Pagination page = ctAccountService.queryCTAccountsWithMarket("1", 0, 10);
        System.out.println(JSON.toJSONString(page));
    }


}
