package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradeScanServiceTest {

    @Autowired
    TradeScanService tradeScanService;

    @Test
    public void queryTopTakePlan() {
        System.out.println(JSON.toJSONString(tradeScanService.queryTopTakePlan()));
    }

    @Test
    public void queryChild() {
        System.out.println(JSON.toJSONString(tradeScanService.queryChild("gv8srws5zpc")));
    }

}
