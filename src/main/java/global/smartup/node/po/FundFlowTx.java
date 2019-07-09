package global.smartup.node.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "fund_flow_tx")
public class FundFlowTx {

    @Id
    @Column(name = "flow_id")
    private String flowId;

    @Id
    @Column(name = "tx_hash")
    private String txHash;

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

}
