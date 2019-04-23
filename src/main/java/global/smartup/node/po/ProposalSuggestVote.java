package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "proposal_suggest_vote")
public class ProposalSuggestVote {


    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "proposal_option_id")
    private Long proposalOptionId;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "stage")
    private String stage;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "market_address")
    private String marketAddress;

    @Column(name = "`index`")
    private Integer index;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "block_time")
    private Date blockTime;




    public Long getProposalOptionId() {
        return proposalOptionId;
    }

    public void setProposalOptionId(Long proposalOptionId) {
        this.proposalOptionId = proposalOptionId;
    }

    public Long getVoteId() {
        return voteId;
    }

    public void setVoteId(Long voteId) {
        this.voteId = voteId;
    }

    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Date blockTime) {
        this.blockTime = blockTime;
    }
}
