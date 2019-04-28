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
        Pagination page = postService.queryPage("t", null, "market","2l05arkk3r4",
                1, 10);
        System.out.println(JSON.toJSONString(page));
    }

    @Test
    public void queryUserCreated() {
        Pagination page = postService.queryUserCreated("1", 1, 10);
        System.out.println(JSON.toJSONString(page));
    }

    @Test
    public void queryUserCollected() {
        Pagination page = postService.queryUserCollected("1", 1, 10);
        System.out.println(JSON.toJSONString(page));
    }

}
