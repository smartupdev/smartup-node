package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "proposal_suggest")
public class ProposalSuggest {

    @Id
    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "proposal_chain_id")
    private String proposalChainId;


    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public String getProposalChainId() {
        return proposalChainId;
    }

    public void setProposalChainId(String proposalChainId) {
        this.proposalChainId = proposalChainId;
    }
}
