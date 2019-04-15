package global.smartup.node.service;


import global.smartup.node.Starter;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.po.Market;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MarketServiceTest {

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private MarketService marketService;

    @Test
    public void add() {
        Market market = new Market();
    }

}
