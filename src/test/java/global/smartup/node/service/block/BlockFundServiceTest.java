package global.smartup.node.service.block;

import global.smartup.node.Starter;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.constract.event.ChargeEvent;
import global.smartup.node.eth.constract.func.ChargeSutFunc;
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
public class BlockFundServiceTest {

    @Autowired
    private BlockFundService blockFundService;

    @Autowired
    private EthClient ethClient;

    @Test
    public void handleChargeSut() {
        Transaction tx = ethClient.getTx("0x9f39aa3b00ba5706e8409433c15514cd9d0e011c94395608d168a43432d66665");
        TransactionReceipt receipt = ethClient.getTxReceipt("0x9f39aa3b00ba5706e8409433c15514cd9d0e011c94395608d168a43432d66665");
        ChargeSutFunc func = ChargeSutFunc.parse(tx);
        ChargeEvent event = ChargeEvent.parse(receipt);
        System.out.println("a");

    }

}
