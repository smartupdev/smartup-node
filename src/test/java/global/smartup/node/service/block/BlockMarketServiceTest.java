package global.smartup.node.service.block;

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
        String txHash = "0x0704dd3c45571279b9c8f1cf1900061421533fc1e15d0ea3cc5f31bd5b6d37cf";
        Transaction tx = ethClient.getTx(txHash);
        CreateMarketFunc func = CreateMarketFunc.parse(tx);

        TransactionReceipt receipt = ethClient.getTxReceipt(txHash);
        CreateMarketEvent event = CreateMarketEvent.parse(receipt);

        System.out.println("end");
    }

}
