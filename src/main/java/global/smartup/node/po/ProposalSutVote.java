package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "proposal_sut_vote")
public class ProposalSutVote {

    @Id
    @Column(name = "proposal_vote_id")
    private Long proposalVoteId;

    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "stage")
    private String stage;

    @Column(name = "market_address")
    private String marketAddress;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "is_agree")
    private Boolean isAgree;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "block_time")
    private Date blockTime;



    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public Long getProposalVoteId() {
        return proposalVoteId;
    }

    public void setProposalVoteId(Long proposalVoteId) {
        this.proposalVoteId = proposalVoteId;
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

    public Boolean getIsAgree() {
        return isAgree;
    }

    public void setIsAgree(Boolean agree) {
        isAgree = agree;
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
