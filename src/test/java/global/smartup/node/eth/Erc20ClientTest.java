package global.smartup.node.eth;

import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Erc20ClientTest {


    @Autowired
    private Erc20Client erc20Client;

    @Test
    public void getSymbol() {
        String s = erc20Client.getSymbol("0xf1899c6eb6940021c1ae4e9c3a8e29ee93704b03");
        System.out.println(s);
    }

}
