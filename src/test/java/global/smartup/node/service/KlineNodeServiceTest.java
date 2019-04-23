package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.po.KlineNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KlineNodeServiceTest {

    @Autowired
    KlineNodeService klineNodeService;

    @Test
    public void queryNodeByTimeId() {

        KlineNode node = klineNodeService.queryNodeByTimeId("0xd8fbc59d5ee1788e80c2919d8d9a18170139e89f", "1hour", "2019_04_15_18");

        System.out.println(JSON.toJSONString(node));
    }

}
