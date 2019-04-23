package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "proposal_option")
public class ProposalOption {

    @Id
    @Column(name = "proposal_option_id")
    private Long proposalOptionId;

    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "`index`")
    private Integer index;

    @Column(name = "text")
    private String text;

    @Column(name = "vote_count")
    private Integer voteCount;



    public Long getProposalOptionId() {
        return proposalOptionId;
    }

    public void setProposalOptionId(Long proposalOptionId) {
        this.proposalOptionId = proposalOptionId;
    }

    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }
}
