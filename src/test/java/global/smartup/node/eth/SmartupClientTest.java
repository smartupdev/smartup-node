package global.smartup.node.eth;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.po.Trade;
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
    public void isTxFail() {
        String txHash = "0xa13d9bf332e4f2de35f50618517d3718195c222b633b602057a6d8b86ba18e41";
        TransactionReceipt receipt = smartupClient.queryReceipt(txHash);
        boolean ret = smartupClient.isTxFail(receipt);
        System.out.println("ret : " + ret);
    }

    @Test
    public void getCtMarketAddress() {
        String txHash = "0xa13d9bf332e4f2de35f50618517d3718195c222b633b602057a6d8b86ba18e41";
        TransactionReceipt receipt = smartupClient.queryReceipt(txHash);
        String ctMarketAddress = smartupClient.getCtMarketAddress(receipt);
        System.out.println("ctMarketAddress : " + ctMarketAddress);
    }

    @Test
    public void getBuyPrice() {
        String txHash = "0x9176544ade43a7ab7ebd9b5fc8a912108b48c56e1312f9456ecb88b5f00d944e";
        TransactionReceipt receipt = smartupClient.queryReceipt(txHash);
        if (receipt != null) {
            Trade trade = smartupClient.getBuyPrice(receipt);
            System.out.println(JSON.toJSONString(trade));
        }
    }

    @Test
    public void getSellPrice() {
        String txHash = "0x7bc4de2789f14da90d290d5032fe50fcefb6dd13ae09c7c7b6778091d1e89c44";
        TransactionReceipt receipt = smartupClient.queryReceipt(txHash);
        if (receipt != null) {
            Trade trade = smartupClient.getSellPrice(receipt);
            System.out.println(JSON.toJSONString(trade));
        }
    }

}
