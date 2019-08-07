package global.smartup.node.service.block;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.constract.event.FirstStageBuyEvent;
import global.smartup.node.eth.constract.func.FirstStageBuyFunc;
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
public class BlockTradeServiceTest {

    @Autowired
    private BlockTradeService blockTradeService;

    @Autowired
    private EthClient ethClient;

    @Test
    public void parseFuncTest() {
        String txHash = "0xfd90bade53ac25da85633078459d5a85e19d8d1b62743c1d39ea8bcfbb250529";
        Transaction tx = ethClient.getTx(txHash);
        FirstStageBuyFunc func = FirstStageBuyFunc.parse(tx);
        System.out.println(JSON.toJSONString(func));
    }

    @Test
    public void parseEventTest() {
        String txHash = "0xfd90bade53ac25da85633078459d5a85e19d8d1b62743c1d39ea8bcfbb250529";
        TransactionReceipt receipt = ethClient.getTxReceipt(txHash);
        FirstStageBuyEvent event = FirstStageBuyEvent.parse(receipt);
        System.out.println(JSON.toJSONString(event));
    }

}
