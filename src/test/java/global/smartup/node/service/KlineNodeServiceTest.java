package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.po.KlineNode;
import global.smartup.node.util.Common;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KlineNodeServiceTest {

    @Autowired
    private KlineNodeService klineNodeService;

    @Test
    public void queryNodeByTimeId() {
        KlineNode node = klineNodeService.queryNodeByTimeId("0xd8fbc59d5ee1788e80c2919d8d9a18170139e89f", "1hour", "2019_04_15_18");
        System.out.println(JSON.toJSONString(node));
    }

    @Test
    public void querySevenDayNode() {
        List<BigDecimal> nodes = klineNodeService.querySevenDayNode("0xe3f1e8F9F3f42a0203c378482B1C218C6FF90F2a");
        System.out.println(JSON.toJSONString(nodes));
    }

    @Test
    public void updateNodeByChain() {
        klineNodeService.updateNodeByChain(
                "123",
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(1),
                Common.parseSimpleTime("2019-05-07 15:30:00"));
    }

}
