package global.smartup.node.eth;

import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExchangeClickTest {

    @Autowired
    private ExchangeClient exchangeClient;

    @Test
    public void querySutBalance() {
        BigDecimal balance = exchangeClient.querySutBalance("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        System.out.println(balance.toPlainString());
    }

    @Test
    public void queryEthBalance() {
        BigDecimal balance = exchangeClient.queryEthBalance("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        System.out.println(balance.toPlainString());
    }

}
