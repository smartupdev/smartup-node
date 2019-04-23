package global.smartup.node.eth;

import global.smartup.node.Starter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlockListenerTest {

    @Autowired
    private BlockListener blockListener;

    @Autowired
    private EthClient ethClient;

    @Test
    public void parseBlock() {

        EthBlock.Block block = ethClient.getBlockByNumber(BigInteger.valueOf(5408855L), true);
        // blockListener.parseBlock(block);

    }




}
