package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.*;
import global.smartup.node.po.*;
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
import java.util.Optional;

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
    private ProposalOptionMapper proposalOptionMapper;

    @Autowired
    private ProposalSuggestMapper proposalSuggestMapper;

    @Autowired
    private ProposalSuggestVoteMapper proposalSuggestVoteMapper;

    @Autowired
    private IdGenerator idGenerator;

    public void saveSutProposal(String userAddress, String marketAddress, String name, String description, BigDecimal sutAmount) {
        marketAddress = Keys.toChecksumAddress(marketAddress);
        userAddress = Keys.toChecksumAddress(userAddress);
        Proposal proposal = queryEditingSutProposal(userAddress, marketAddress);
        if (proposal == null) {
            proposal = new Proposal();
            Long id = idGenerator.getId();
            proposal.setProposalId(id);
            proposal.setUserAddress(userAddress);
            proposal.setMarketAddress(marketAddress);
            proposal.setName(name);
            proposal.setDescription(description);
            proposal.setType(PoConstant.Proposal.Type.Sut);
            proposal.setIsFinished(false);
            proposal.setStage(PoConstant.TxStage.Creating);
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
        marketAddress = Keys.toChecksumAddress(marketAddress);
        userAddress = Keys.toChecksumAddress(userAddress);
        Proposal proposal = queryEditingSuggestProposal(userAddress, marketAddress);
        if (proposal == null) {
            proposal = new Proposal();
            Long id = idGenerator.getId();
            proposal.setProposalId(id);
            proposal.setUserAddress(userAddress);
            proposal.setMarketAddress(marketAddress);
            proposal.setName(name);
            proposal.setDescription(description);
            proposal.setType(PoConstant.Proposal.Type.Suggest);
            proposal.setIsFinished(false);
            proposal.setStage(PoConstant.TxStage.Creating);
            proposal.setCreateTime(new Date());
            proposalMapper.insert(proposal);
            for (int i = 0; i < options.size(); i++) {
                ProposalOption option = new ProposalOption();
                option.setIndex(i + 1);
                option.setText(options.get(i));
                option.setProposalId(id);
                option.setProposalOptionId(idGenerator.getId());
                option.setVoteCount(0);
                proposalOptionMapper.insert(option);
            }
        } else {
            proposal.setName(name);
            proposal.setDescription(description);
            proposalMapper.updateByPrimaryKey(proposal);
            // del
            ProposalOption cdt = new ProposalOption();
            cdt.setProposalId(proposal.getProposalId());
            proposalOptionMapper.delete(cdt);
            // add
            for (int i = 0; i < options.size(); i++) {
                ProposalOption option = new ProposalOption();
                option.setIndex(i+1);
                option.setText(options.get(i));
                option.setProposalId(proposal.getProposalId());
                option.setProposalOptionId(idGenerator.getId());
                option.setVoteCount(0);
                proposalOptionMapper.insert(option);
            }
        }
    }

    public void saveSutVote(String txHash, Long proposalId, String userAddress, String marketAddress, Boolean isAgree) {
        ProposalSutVote vote = querySutVoteByTxHash(txHash);
        if (vote == null) {
            vote = new ProposalSutVote();
            vote.setTxHash(txHash);
            vote.setProposalId(proposalId);
            vote.setProposalVoteId(idGenerator.getId());
            vote.setUserAddress(userAddress);
            vote.setMarketAddress(marketAddress);
            vote.setIsAgree(isAgree);
            vote.setStage(PoConstant.TxStage.Pending);
            vote.setCreateTime(new Date());
            proposalSutVoteMapper.insert(vote);
        } else {
            if (PoConstant.TxStage.isFinish(vote.getStage())) {
                return;
            }
            vote.setTxHash(txHash);
            vote.setProposalId(proposalId);
            vote.setUserAddress(userAddress);
            vote.setMarketAddress(marketAddress);
            vote.setIsAgree(isAgree);
            vote.setStage(PoConstant.TxStage.Pending);
            vote.setCreateTime(new Date());
            proposalSutVoteMapper.updateByPrimaryKey(vote);
        }
    }

    // proposal sut create
    public void updateSutProposalCreatedFromChain(String txHash, boolean isSuccess, String userAddress,
                                                  String marketAddress, BigDecimal sutAmount, Date blockTime) {
        Proposal proposal = queryEditingSutProposal(userAddress, marketAddress);
        if (proposal == null) {
            return;
        }
        proposal.setTxHash(txHash);
        if (isSuccess) {
            proposal.setStage(PoConstant.TxStage.Success);
        } else {
            proposal.setStage(PoConstant.TxStage.Fail);
        }
        proposal.setBlockTime(blockTime);
        proposal.getProposalSut().setSutAmount(sutAmount);
        proposalMapper.updateByPrimaryKey(proposal);
        proposalSutMapper.updateByPrimaryKey(proposal.getProposalSut());
    }

    // proposal sut vote
    public void updateSutProposalVoteFromChain(String txHash, boolean isSuccess, String userAddress,
                                               String marketAddress, Boolean isAgree, Date blockTime) {
        ProposalSutVote vote = querySutVoteByTxHash(txHash);
        if (vote == null) {
            Proposal proposal = queryLastSutProposal(marketAddress);
            Long voteId = idGenerator.getId();

            vote = new ProposalSutVote();
            vote.setProposalId(proposal.getProposalId());
            vote.setProposalVoteId(voteId);
            vote.setTxHash(txHash);
            vote.setUserAddress(userAddress);
            vote.setMarketAddress(marketAddress);
            vote.setIsAgree(isAgree);
            if (isSuccess) {
                vote.setStage(PoConstant.TxStage.Success);
            } else {
                vote.setStage(PoConstant.TxStage.Fail);
            }
            vote.setCreateTime(new Date());
            vote.setBlockTime(blockTime);
            proposalSutVoteMapper.insert(vote);
        } else {
            vote.setUserAddress(userAddress);
            vote.setMarketAddress(marketAddress);
            vote.setIsAgree(isAgree);
            if (isSuccess) {
                vote.setStage(PoConstant.TxStage.Success);
            } else {
                vote.setStage(PoConstant.TxStage.Fail);
            }
            vote.setBlockTime(blockTime);
            proposalSutVoteMapper.updateByPrimaryKey(vote);
        }
    }

    // proposal sut finish
    public void updateSutProposalFinishFromChain(String txHash, boolean isSuccess, String userAddress,
                                                 String marketAddress, Boolean isAgree) {
        if (isSuccess) {
            Proposal proposal = queryLastSutProposal(marketAddress);
            proposal.setIsFinished(true);
            proposalMapper.updateByPrimaryKey(proposal);
            proposal.getProposalSut().setIsSuccess(isAgree);
            proposalSutMapper.updateByPrimaryKey(proposal.getProposalSut());
        }
    }

    // proposal suggest create
    public void updateSuggestProposalCreatedFromChain(String txHash, boolean isSuccess, String userAddress,
                                                      String marketAddress, String proposalChainId, Date blockTime) {
        Proposal proposal = queryEditingSuggestProposal(userAddress, marketAddress);
        if (proposal == null) {
            return;
        }
        proposal.setTxHash(txHash);
        if (isSuccess) {
            proposal.setStage(PoConstant.TxStage.Success);
        } else {
            proposal.setStage(PoConstant.TxStage.Fail);
        }
        proposal.setBlockTime(blockTime);
        proposalMapper.updateByPrimaryKey(proposal);

        ProposalSuggest suggest = new ProposalSuggest();
        suggest.setProposalId(proposal.getProposalId());
        suggest.setProposalChainId(proposalChainId);
        proposalSuggestMapper.insert(suggest);
    }

    // proposal suggest vote
    public void updateSuggestProposalVoteFromChain(String txHash, boolean isSuccess, String userAddress,
                                                   String marketAddress, String proposalChainId, Integer index, Date blockTime) {
        if (isSuggestProposalVoteExist(txHash)) {
            return;
        }
        Proposal proposal = querySuggestProposalByChainId(proposalChainId);
        if (proposal == null) {
            log.error("Can not find proposal suggest by chainId = {}", proposalChainId);
            return;
        }
        Optional<ProposalOption> optional = proposal.getOptions().stream().filter(o -> o.getIndex().compareTo(index) == 0).findFirst();
        if (!optional.isPresent()) {
            log.error("Can not find proposal suggest option by chainId = {}, index = ", proposalChainId, index);
            return;
        }
        ProposalOption option = optional.get();

        ProposalSuggestVote vote = new ProposalSuggestVote();
        vote.setVoteId(idGenerator.getId());
        vote.setTxHash(txHash);
        if (isSuccess) {
            vote.setStage(PoConstant.TxStage.Success);
        } else {
            vote.setStage(PoConstant.TxStage.Fail);
        }
        vote.setUserAddress(userAddress);
        vote.setMarketAddress(marketAddress);
        vote.setProposalId(proposal.getProposalId());
        vote.setBlockTime(blockTime);
        vote.setProposalOptionId(option.getProposalOptionId());
        vote.setIndex(index);
        proposalSuggestVoteMapper.insert(vote);

        // update option
        option.setVoteCount(option.getVoteCount() + 1);
        proposalOptionMapper.updateByPrimaryKey(option);
    }

    // proposal suggest finish
    public void updateSuggestProposalFinishFromChain(String txHash, boolean isSuccess, String userAddress,
                                                     String marketAddress, String proposalChainId) {
        if (isSuccess) {
            Proposal proposal = querySuggestProposalByChainId(proposalChainId);
            if (proposal == null) {
                log.error("Can not find proposal suggest by chainId = {}", proposalChainId);
                return;
            }
            proposal.setIsFinished(true);
            proposalMapper.updateByPrimaryKey(proposal);
        }
    }

    public boolean isProposalIdExist(Long proposalId) {
        return proposalMapper.selectByPrimaryKey(proposalId) != null;
    }

    public Proposal queryEditingSutProposal(String userAddress, String marketAddress) {
        Proposal proposal = queryEditingProposal(userAddress, marketAddress, PoConstant.Proposal.Type.Sut);
        if (proposal != null) {
            ProposalSut proposalSut = proposalSutMapper.selectByPrimaryKey(proposal.getProposalId());
            proposal.setProposalSut(proposalSut);
        }
        return proposal;
    }

    public Proposal queryEditingSuggestProposal(String userAddress, String marketAddress) {
        Proposal proposal = queryEditingProposal(userAddress, marketAddress, PoConstant.Proposal.Type.Suggest);
        if (proposal != null) {
            proposal.setOptions(queryOptions(proposal.getProposalId()));
        }
        return proposal;
    }

    public Proposal queryLastSutProposal(String marketAddress) {
        Proposal proposal = queryLastCreated(marketAddress, PoConstant.Proposal.Type.Sut);
        if (proposal != null) {
            ProposalSut sut = proposalSutMapper.selectByPrimaryKey(proposal.getProposalId());
            proposal.setProposalSut(sut);
        }
        return proposal;
    }

    public Proposal queryLastCreated(String marketAddress, String type) {
        Example example = new Example(Proposal.class);
        example.createCriteria()
                .andEqualTo("marketAddress", marketAddress)
                .andEqualTo("type", type)
                .andEqualTo("stage", PoConstant.TxStage.Success);
        example.orderBy("createTime").desc();
        Page<Proposal> page = PageHelper.startPage(1, 1, false);
        proposalMapper.selectByExample(example);
        if (page.getResult().size() <= 0) {
            return null;
        }
        return page.getResult().get(0);
    }

    public Proposal queryEditingProposal(String userAddress, String marketAddress, String type) {
        marketAddress = Keys.toChecksumAddress(marketAddress);
        userAddress = Keys.toChecksumAddress(userAddress);
        Proposal cdt = new Proposal();
        cdt.setStage(PoConstant.TxStage.Creating);
        cdt.setType(type);
        cdt.setUserAddress(userAddress);
        cdt.setMarketAddress(marketAddress);
        return proposalMapper.selectOne(cdt);
    }

    public ProposalSutVote querySutVoteByTxHash(String txHash) {
        ProposalSutVote cdt = new ProposalSutVote();
        cdt.setTxHash(txHash);
        return proposalSutVoteMapper.selectOne(cdt);
    }

    public List<ProposalOption> queryOptions(Long proposalId) {
        Example example = new Example(ProposalOption.class);
        example.createCriteria().andEqualTo("proposalId", proposalId);
        example.orderBy("index").asc();
        return proposalOptionMapper.selectByExample(example);
    }

    public boolean isSuggestProposalVoteExist(String txHash) {
        ProposalSuggestVote cdt = new ProposalSuggestVote();
        cdt.setTxHash(txHash);
        return proposalSuggestVoteMapper.selectOne(cdt) != null;
    }

    public List<ProposalSutVote> queryProposalSutVote(Long proposalId) {
        Example example = new Example(ProposalSuggestVote.class);
        example.createCriteria().andEqualTo("proposalId", proposalId);
        example.orderBy("createTime").asc();
        return proposalSutVoteMapper.selectByExample(example);
    }

    public Proposal querySuggestProposalByChainId(String chainId) {
        ProposalSuggest cdt = new ProposalSuggest();
        cdt.setProposalChainId(chainId);
        ProposalSuggest suggest = proposalSuggestMapper.selectOne(cdt);
        if (suggest == null) {
            return null;
        }
        Proposal proposal = proposalMapper.selectByPrimaryKey(suggest.getProposalId());
        if (proposal == null) {
            return null;
        }
        proposal.setProposalSuggest(suggest);

        Example optionExample = new Example(ProposalOption.class);
        optionExample.createCriteria().andEqualTo("proposalId", proposal.getProposalId());
        optionExample.orderBy("index").asc();
        List<ProposalOption> options = proposalOptionMapper.selectByExample(optionExample);
        proposal.setOptions(options);

        return proposal;
    }

    public Proposal queryOne(Long proposalId) {
        Proposal proposal = proposalMapper.selectByPrimaryKey(proposalId);
        if (proposal == null) {
            return null;
        }
        if (PoConstant.Proposal.Type.Sut.equals(proposal.getType())) {
            proposal.setProposalSut(proposalSutMapper.selectByPrimaryKey(proposal.getProposalId()));
            proposal.setSutVotes(queryProposalSutVote(proposalId));
        } else if (PoConstant.Proposal.Type.Suggest.equals(proposal.getType())) {
            proposal.setProposalSuggest(proposalSuggestMapper.selectByPrimaryKey(proposalId));
            proposal.setOptions(queryOptions(proposalId));
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
                .andEqualTo("stage", PoConstant.TxStage.Success);
        example.orderBy("createTime").desc();
        Page<Proposal> page = PageHelper.startPage(pageNumb, pageSize);
        proposalMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

}
