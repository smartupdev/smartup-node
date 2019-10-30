package global.smartup.node.eth;


import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EthClientTest {

    @Autowired
    private EthClient ethClient;


    @Test
    public void getBlockNumber() {
        BigInteger num = ethClient.getLastBlockNumber();
        System.out.println("current number is " + num.toString());
    }

    @Test
    public void getBlockByNumber() {
        EthBlock.Block block = ethClient.getBlockByNumber(BigInteger.valueOf(5389017L), true);
        List<EthBlock.TransactionResult> list =  block.getTransactions();
        for (EthBlock.TransactionResult result : list) {
            Transaction tx = (Transaction) result.get();
            if (tx.getHash().equalsIgnoreCase("0x7e375284ae14e7589428ae7cf43a09dca2fa3e46c4f26f24f8f8de81e5eac87b")) {
                String input = tx.getInput();

                // Numeric.toHexString()
                System.out.println("aa");
            }
        }

    }

    @Test
    public void getBalance() {
        BigDecimal d = ethClient.getBalance("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        System.out.println("balance : " + d.toPlainString());
    }

    @Test
    public void getTx() {
        Transaction tx = ethClient.getTx("0xa13d9bf332e4f2de35f50618517d3718195c222b633b602057a6d8b86ba18e41");
        System.out.println("1");
    }

    @Test
    public void getTxReceipt() {
        ethClient.getTxReceipt("0xa13d9bf332e4f2de35f50618517d3718195c222b633b602057a6d8b86ba18e41");
    }

    @Test
    public void getTransactionCount() {
        BigInteger c = ethClient.getTransactionCount("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        System.out.println("count : " + c);
    }

}
