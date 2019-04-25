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

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReplyServiceTest {

    @Autowired
    private ReplyService replyService;

    @Test
    public void queryPage() {
        Pagination page = replyService.queryPage("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E", 8424213186613248L, 1, 10);
        System.out.println(JSON.toJSONString(page));
    }

}
