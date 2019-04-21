package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "proposal_sut_vote")
public class ProposalSutVote {

    @Id
    @Column(name = "proposal_vote_id")
    private String proposalVoteId;

    @Column(name = "proposal_id")
    private String proposalId;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "stage")
    private String stage;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "is_agree")
    private String isAgree;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "block_time")
    private String blockTime;


    public String getProposalVoteId() {
        return proposalVoteId;
    }

    public void setProposalVoteId(String proposalVoteId) {
        this.proposalVoteId = proposalVoteId;
    }

    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
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

    public String getIsAgree() {
        return isAgree;
    }

    public void setIsAgree(String isAgree) {
        this.isAgree = isAgree;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(String blockTime) {
        this.blockTime = blockTime;
    }
}
