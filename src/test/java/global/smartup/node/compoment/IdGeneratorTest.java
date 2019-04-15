package global.smartup.node.compoment;


import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IdGeneratorTest {

    @Autowired
    IdGenerator idGenerator;

    @Test
    public void test() {
        long id = idGenerator.getId();
        String hex = Long.toString(id, 36);
        System.out.println(hex);
    }

}
