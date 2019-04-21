package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.ProposalMapper;
import global.smartup.node.mapper.ProposalSutMapper;
import global.smartup.node.mapper.ProposalSutVoteMapper;
import global.smartup.node.po.Proposal;
import global.smartup.node.po.ProposalSut;
import global.smartup.node.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class ProposalService {

    private static final Logger log = LoggerFactory.getLogger(ProposalService.class);

    @Autowired
    private ProposalMapper proposalMapper;

    @Autowired
    private ProposalSutMapper proposalSutMapper;

    @Autowired
    private ProposalSutVoteMapper proposalSutVoteMapper;

    @Autowired
    private IdGenerator idGenerator;

    public void saveSutProposal(String userAddress, String marketAddress, String name, String description, BigDecimal sutAmount) {
        marketAddress = Keys.toChecksumAddress(marketAddress);
        userAddress = Keys.toChecksumAddress(userAddress);
        Proposal proposal = queryCurrentSutProposal(userAddress, marketAddress);
        if (proposal == null) {
            proposal = new Proposal();
            Long id = idGenerator.getId();
            proposal.setProposalId(id);
            proposal.setUserAddress(userAddress);
            proposal.setMarketAddress(marketAddress);
            proposal.setName(name);
            proposal.setDescription(description);
            proposal.setType(PoConstant.Proposal.Type.Sut);
            proposal.setStage(PoConstant.Proposal.Stage.Creating);
            proposal.setCreateTime(new Date());
            ProposalSut sut = new ProposalSut();
            sut.setProposalId(id);
            sut.setSutAmount(sutAmount);
            proposalMapper.insert(proposal);
            proposalSutMapper.insert(sut);
        } else {
            proposal.setName(name);
            proposal.setDescription(description);
            proposal.getProposalSut().setSutAmount(sutAmount);
            proposalMapper.updateByPrimaryKey(proposal);
            proposalSutMapper.updateByPrimaryKey(proposal.getProposalSut());
        }

    }

    public void saveSuggestProposal(String userAddress, String marketAddress, String name, String description, List<String> options) {

    }

    // proposal sut 创建完成
    public void updateSutProposalCreatedFromChain(String txHash, boolean isSuccess, String userAddress, String marketAddress, BigDecimal sutAmount, Date blockTime) {
        Proposal proposal = queryCurrentSutProposal(userAddress, marketAddress);
        if (proposal == null) {
            return;
        }
        proposal.setTxHash(txHash);
        if (isSuccess) {
            proposal.setStage(PoConstant.Proposal.Stage.Voting);
        } else {
            proposal.setStage(PoConstant.Proposal.Stage.Fail);
        }
        proposal.setBlockTime(blockTime);
        proposal.getProposalSut().setSutAmount(sutAmount);
        proposalMapper.updateByPrimaryKey(proposal);
        proposalSutMapper.updateByPrimaryKey(proposal.getProposalSut());
    }

    public void updateSuggestProposalFromChain() {

    }

    public Proposal queryCurrentSutProposal(String userAddress, String marketAddress) {
        return queryCurrentProposal(userAddress, marketAddress, PoConstant.Proposal.Type.Sut);
    }

    public Proposal queryCurrentSuggestProposal(String userAddress, String marketAddress) {
        return queryCurrentProposal(userAddress, marketAddress, PoConstant.Proposal.Type.Suggest);
    }

    public Proposal queryCurrentProposal(String userAddress, String marketAddress, String type) {
        Proposal cdt = new Proposal();
        cdt.setType(type);
        cdt.setUserAddress(userAddress);
        cdt.setMarketAddress(marketAddress);
        Proposal proposal = proposalMapper.selectOne(cdt);
        if (proposal != null && PoConstant.Proposal.Type.Sut.equals(type)) {
            ProposalSut proposalSut = proposalSutMapper.selectByPrimaryKey(proposal.getProposalId());
            proposal.setProposalSut(proposalSut);
        }
        return proposal;
    }


    public Pagination<Proposal> queryUserProposalPage(String userAddress, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Proposal.class);
        example.createCriteria().andEqualTo("userAddress", userAddress);
        example.orderBy("createTime").desc();
        Page<Proposal> page = PageHelper.startPage(pageNumb, pageSize);
        proposalMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Proposal> queryMarketProposalPage(String marketAddress, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Proposal.class);
        example.createCriteria()
                .andEqualTo("marketAddress", marketAddress)
                .andEqualTo("stage", PoConstant.Proposal.Stage.Finished);
        example.orderBy("createTime").desc();
        Page<Proposal> page = PageHelper.startPage(pageNumb, pageSize);
        proposalMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

}
