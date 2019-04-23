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
import java.util.Arrays;

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
    public void saveSuggestProposal() {
        proposalService.saveSuggestProposal("user", "market", "name haha", "desc", Arrays.asList("11", "22"));
    }

    @Test
    public void queryEditingSutProposal() {
        Proposal proposal = proposalService.queryEditingSutProposal("user", "market");
        System.out.println(JSON.toJSON(proposal));
    }

    @Test
    public void queryEditingSuggestProposal() {
        Proposal proposal = proposalService.queryEditingSuggestProposal("user", "market");
        System.out.println(JSON.toJSONString(proposal));
    }

    @Test
    public void queryUserProposalPage() {
        Pagination page = proposalService.queryUserProposalPage("user", 1, 1);
        System.out.println(JSON.toJSON(page));
    }

    @Test
    public void queryPage() {
        Pagination page = proposalService.queryMarketProposalPage("market", 1, 10);
        System.out.println(JSON.toJSON(page));
    }


    @Test
    public void queryLastSutProposal() {
        Proposal proposal = proposalService.queryLastSutProposal("0xDD6413EC9059AD4f0125B9e1e35402A5f7781EBc");
        System.out.println(JSON.toJSONString(proposal));
    }


}
