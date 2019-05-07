package global.smartup.node.service;

import global.smartup.node.Starter;
import global.smartup.node.constant.PoConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Test
    public void query() {
        transactionService.query("");
    }

    @Test
    public void isLastTradeTransactionInSegment() {
        boolean b = transactionService.isLastTradeTransactionInSegment("", new Date(), PoConstant.KLineNode.Segment.Hour);
    }

}
