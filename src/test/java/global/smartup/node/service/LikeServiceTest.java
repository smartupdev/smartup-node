package global.smartup.node.service;

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
public class LikeServiceTest {

    @Autowired
    private LikeService likeService;

    @Test
    public void addPostLike() {
        likeService.addPostLike("user", "market", 1L);

    }

    @Test
    public void addPostDislike() {
        likeService.addPostDislike("user", "market", 1L);
    }


}
