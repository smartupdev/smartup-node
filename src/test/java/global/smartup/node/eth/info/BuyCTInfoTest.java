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
public class BuyCTInfoTest {

    @Autowired
    private EthClient ethClient;

    @Test
    public void parseTransaction() {
        Transaction tx = ethClient.getTx("0xfc78e39b585a52cc309273ea008f5471375b377374dcd5550a4f12aeddd5b172");
        CTBuyInfo info = new CTBuyInfo();
        info.parseTransaction(tx);
        System.out.println(JSON.toJSONString(info));
    }

    @Test
    public void parseTransactionReceipt() {
        TransactionReceipt receipt = ethClient.getTxReceipt("0xfc78e39b585a52cc309273ea008f5471375b377374dcd5550a4f12aeddd5b172");
        CTBuyInfo info = new CTBuyInfo();
        info.parseTransactionReceipt(receipt);
        System.out.println(JSON.toJSONString(info));
    }
}
