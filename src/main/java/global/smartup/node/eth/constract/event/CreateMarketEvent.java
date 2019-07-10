package global.smartup.node.eth.constract.event;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;

public class CreateMarketEvent {

    // 事件1

    private String ctAddress;

    private String marketCreator;

    private BigDecimal initialDeposit;

    // 事件2

    private String owner;

    private BigDecimal sutRemain;

    private BigDecimal ethRemain;


    public static CreateMarketEvent parse(TransactionReceipt receipt) {
        CreateMarketEvent event = new CreateMarketEvent();

        return event;
    }


    public String getCtAddress() {
        return ctAddress;
    }

    public void setCtAddress(String ctAddress) {
        this.ctAddress = ctAddress;
    }

    public String getMarketCreator() {
        return marketCreator;
    }

    public void setMarketCreator(String marketCreator) {
        this.marketCreator = marketCreator;
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getSutRemain() {
        return sutRemain;
    }

    public void setSutRemain(BigDecimal sutRemain) {
        this.sutRemain = sutRemain;
    }

    public BigDecimal getEthRemain() {
        return ethRemain;
    }

    public void setEthRemain(BigDecimal ethRemain) {
        this.ethRemain = ethRemain;
    }
}
