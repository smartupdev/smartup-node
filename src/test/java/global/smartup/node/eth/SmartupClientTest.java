package global.smartup.node.eth;

import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmartupClientTest {

    @Autowired
    private SmartupClient smartupClient;

    // 0xa13d9bf332e4f2de35f50618517d3718195c222b633b602057a6d8b86ba18e41 => 0x58B0F0Dc3B095a17CF390D0172F7E1a26B069392

    @Test
    public void getCtMarketAddress() {
        String txHash = "0xa13d9bf332e4f2de35f50618517d3718195c222b633b602057a6d8b86ba18e41";
        TransactionReceipt receipt = smartupClient.queryReceipt(txHash);
        String ctMarketAddress = smartupClient.getCtMarketAddress(receipt);
        System.out.println("ctMarketAddress : " + ctMarketAddress);
    }
}
