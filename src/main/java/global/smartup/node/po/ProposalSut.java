package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name = "proposal_sut")
public class ProposalSut {

    @Id
    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "sut_amount")
    private BigDecimal sutAmount;

    @Column(name = "is_success")
    private Boolean isSuccess;


    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public BigDecimal getSutAmount() {
        return sutAmount;
    }

    public void setSutAmount(BigDecimal sutAmount) {
        this.sutAmount = sutAmount;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean success) {
        isSuccess = success;
    }
}
