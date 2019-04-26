package global.smartup.node.service;

import global.smartup.node.Starter;
import global.smartup.node.constant.PoConstant;
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
    public void addMark() {
        likeService.addMark("user", "market", PoConstant.Liked.Type.Post, false, "2");
    }

    @Test
    public void delMark() {
        likeService.delMark("user", "market", PoConstant.Liked.Type.Post, "1");
    }


}