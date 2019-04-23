package global.smartup.node.controller;

import global.smartup.node.po.Proposal;
import global.smartup.node.service.ProposalService;
import global.smartup.node.util.Pagination;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;

@Api(description = "提案")
@RestController
@RequestMapping("/api")
public class ProposalController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ProposalController.class);

    @Autowired
    private ProposalService proposalService;

    @ApiOperation(value = "保存SUT提案", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：marketAddress, name, description, sutAmount\n" +
                    "返回：是否成功")
    @RequestMapping("/user/proposal/sut/save")
    public Object saveProposalSut(HttpServletRequest request, String marketAddress, String name, String description, BigDecimal sutAmount) {
        try {
            String userAddress = getLoginUserAddress(request);
            proposalService.saveSutProposal(userAddress, marketAddress, name, description, sutAmount);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户当前编辑的SUT提案", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：marketAddress\n" +
                    "返回：obj = {见/api/proposal/one}")
    @RequestMapping("/user/proposal/sut/editing")
    public Object editingProposalSut(HttpServletRequest request, String marketAddress) {
        try {
            String userAddress = getLoginUserAddress(request);
            Proposal proposal = proposalService.queryEditingSutProposal(userAddress, marketAddress);
            return Wrapper.success(proposal);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "保存suggest提案", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：marketAddress, name, description, options( options='red' options='yellow' .. )\n" +
                    "返回：是否成功")
    @RequestMapping("/user/proposal/suggest/save")
    public Object saveProposalSuggest(HttpServletRequest request, String marketAddress, String name, String description, String[] options) {
        try {
            proposalService.saveSuggestProposal(marketAddress, getLoginUserAddress(request), name, description, Arrays.asList(options));
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户当前编辑的suggest提案", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：marketAddress\n" +
                    "返回：obj = {见/api/proposal/one}")
    @RequestMapping("/user/proposal/suggest/editing")
    public Object editingProposalSuggest(HttpServletRequest request, String marketAddress) {
        try {
            String userAddress = getLoginUserAddress(request);
            Proposal proposal = proposalService.queryEditingSuggestProposal(userAddress, marketAddress);
            return Wrapper.success(proposal);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户提案列表", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：pageNumb, pageSize \n" +
                    "返回：obj = { list = [ {见/api/proposal/one}, ... ] }")
    @RequestMapping("/user/proposal/list")
    public Object userProposalList(HttpServletRequest request, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            Pagination page = proposalService.queryUserProposalPage(userAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "提案详情", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：proposalId\n" +
                        "返回：obj = {\n" +
                        "　proposalId, txHash, type(sut/suggest), stage(creating, pending, success, fail), marketAddress, \n" +
                        "　userAddress, name, description, isFinished, createTime, blockTime, \n" +
                        "　type = sut：\n" +
                        "　　proposalSut = { proposalId, sutAmount, isSuccess }\n" +
                        "　　sutVotes = [{ proposalVoteId, proposalId, txHash, stage, marketAddress, userAddress, isAgree, createTime, blockTime }, ...]\n" +
                        "　type = suggest：\n" +
                        "　　proposalSuggest = { proposalId, proposalChainId }\n" +
                        "　　options = { proposalOptionId, proposalId, index, text, voteCount }\n" +
                        "}")
    @RequestMapping("/proposal/one")
    public Object proposalOne(HttpServletRequest request, Long proposalId) {
        try {
            Proposal proposal = proposalService.queryOne(proposalId);
            return Wrapper.success(proposal);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "市场提案列表", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：marketAddress, pageNumb, pageSize\n" +
                    "返回：obj = { list = [ {见/api/proposal/one}, ... ] }")
    @RequestMapping("/market/proposal/list")
    public Object marketProposalList(HttpServletRequest request, String marketAddress, Integer pageNumb, Integer pageSize) {
        try {
            Pagination page = proposalService.queryMarketProposalPage(marketAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

}
