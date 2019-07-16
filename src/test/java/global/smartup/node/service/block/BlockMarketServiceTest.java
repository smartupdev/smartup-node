package global.smartup.node.service.block;

import global.smartup.node.Starter;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.constract.func.CreateMarketFunc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.Transaction;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlockMarketServiceTest {

    @Autowired
    private BlockMarketService blockMarketService;

    @Autowired
    private EthClient ethClient;

    @Test
    public void parseTx() {
        String txHash = "0x8f6896763cc2635db19ebca53ffa064ba2d4437c2085f7ec42017be182c7bbc8";
        Transaction tx = ethClient.getTx(txHash);
        CreateMarketFunc func = CreateMarketFunc.parse(tx);
        System.out.println("");
    }

}
