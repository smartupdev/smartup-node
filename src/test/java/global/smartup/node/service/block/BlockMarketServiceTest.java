package global.smartup.node.service.block;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.constract.event.CreateMarketEvent;
import global.smartup.node.eth.constract.func.CreateMarketFunc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

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
        String txHash = "0xb605570d63d41f6b63490344249880ab043ce25e61b254c96087c51f87328908";
        Transaction tx = ethClient.getTx(txHash);
        CreateMarketFunc func = CreateMarketFunc.parse(tx);
        System.out.println(JSON.toJSONString(func));

        TransactionReceipt receipt = ethClient.getTxReceipt(txHash);
        CreateMarketEvent event = CreateMarketEvent.parse(receipt);
        System.out.println(JSON.toJSONString(event));
    }

}
