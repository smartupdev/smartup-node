package global.smartup.node.eth;

import com.alibaba.fastjson.JSON;

import global.smartup.node.Starter;
import global.smartup.node.po.Trade;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.util.Date;

@ActiveProfiles("unit")
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
    public void getBlockTime() {
        String txHash = "0x9176544ade43a7ab7ebd9b5fc8a912108b48c56e1312f9456ecb88b5f00d944e";
        TransactionReceipt receipt = smartupClient.queryReceipt(txHash);
        if (receipt != null) {
            Date date = smartupClient.getBlockTime(receipt);
            System.out.println(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(date));
        }
    }

    @Test
    public void getCtBalance() {
        BigDecimal b = smartupClient.getCtBalance("0x27bd0b63fbe4799ad60bd92159b308ecab6a9059", "0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        System.out.println(b.toPlainString());
    }

    @Test
    public void getSutBalance() {
        BigDecimal b = smartupClient.getSutBalance("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        if (b != null) {
            System.out.println(b.toPlainString());
        }
    }

}
