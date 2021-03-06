package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.po.User;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private IdGenerator idGenerator;

    @Test
    public void query() {
        User user = userService.query("123");
        System.out.println(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(user.getCreateTime()));
        System.out.println(JSON.toJSONString(user));
    }


    @Test
    public void code() {
        long numb = idGenerator.getId();
        String hex = Long.toHexString(numb);
        System.out.println(hex);
    }
}
