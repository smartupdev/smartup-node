package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import global.smartup.node.Starter;
import global.smartup.node.po.Proposal;
import global.smartup.node.util.Pagination;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProposalServiceTest {


    @Autowired
    private ProposalService proposalService;


    @Test
    public void saveSutProposal() {
        proposalService.saveSutProposal("user1", "market", "p name", "desc", BigDecimal.ZERO);
    }

    @Test
    public void queryCurrentSutProposal() {
        Proposal proposal = proposalService.queryCurrentSutProposal("user", "market");
        System.out.println(JSON.toJSON(proposal));
    }

    @Test
    public void queryMyProposal() {
        Pagination page = proposalService.queryUserProposalPage("user", 1, 1);
        System.out.println(JSON.toJSON(page));
    }

    @Test
    public void queryPage() {
        Pagination page = proposalService.queryMarketProposalPage("market", 1, 10);
        System.out.println(JSON.toJSON(page));
    }



}
