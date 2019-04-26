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
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @Test
    public void queryPage() {
        Pagination page = postService.queryPage("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E", "market",
                "0xB79b9386a7D2647FfbeACaAA63D28c5C78b509A7", 1, 10);
        System.out.println(JSON.toJSONString(page));
    }
}
