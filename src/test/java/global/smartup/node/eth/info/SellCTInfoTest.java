package global.smartup.node.eth.info;

import com.alibaba.fastjson.JSON;

import global.smartup.node.Starter;
import global.smartup.node.eth.EthClient;
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
public class SellCTInfoTest {

    @Autowired
    private EthClient ethClient;

    @Test
    public void parseTransaction() {
        Transaction tx = ethClient.getTx("0xfe2659ffc1d351dfb08d3fb064ea6a2885671249108d784d3c94dd0c2fa6075d");
        SellCTInfo sellCTInfo = new SellCTInfo();
        sellCTInfo.parseTransaction(tx);
        System.out.println(JSON.toJSONString(sellCTInfo));
    }

    @Test
    public void parseTransactionReceipt() {
        TransactionReceipt receipt = ethClient.getTxReceipt("0xfe2659ffc1d351dfb08d3fb064ea6a2885671249108d784d3c94dd0c2fa6075d");
        SellCTInfo sellCTInfo = new SellCTInfo();
        sellCTInfo.parseTransactionReceipt(receipt);
        System.out.println(JSON.toJSONString(sellCTInfo));
    }

}
