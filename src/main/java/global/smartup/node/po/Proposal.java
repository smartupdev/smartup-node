package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Table(name = "proposal")
public class Proposal {

    @Id
    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "type")
    private String type;

    @Column(name = "stage")
    private String stage;

    @Column(name = "market_address")
    private String marketAddress;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "block_time")
    private Date blockTime;

    @Transient
    private ProposalSut proposalSut;

    @Transient
    private List<ProposalSutVote> sutVotes;


    public List<ProposalSutVote> getSutVotes() {
        return sutVotes;
    }

    public void setSutVotes(List<ProposalSutVote> sutVotes) {
        this.sutVotes = sutVotes;
    }

    public ProposalSut getProposalSut() {
        return proposalSut;
    }

    public void setProposalSut(ProposalSut proposalSut) {
        this.proposalSut = proposalSut;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getMarketAddress() {
        return marketAddress;
    }

    public void setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
