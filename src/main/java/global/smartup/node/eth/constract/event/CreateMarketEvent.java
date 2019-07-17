package global.smartup.node.eth.constract.event;

import global.smartup.node.constant.BuConstant;
import global.smartup.node.eth.constract.Const;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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
        try {
            if (receipt == null) {
                return null;
            }
            List<Log> list = receipt.getLogs();
            Log logA = null, logB = null;
            for (Log log : list) {
                if (log.getTopics().contains(Const.Exchange.Event.CreateMarketA)) {
                    logA = log;
                }
                if (log.getTopics().contains(Const.Exchange.Event.CreateMarketB)) {
                    logB = log;
                }
            }
            if (logA == null || logB == null) {
                return null;
            }

            // parse log a
            String dataA = logA.getData();
            List<Type> paramsA =  FunctionReturnDecoder.decode(dataA, Arrays.asList(new TypeReference[]{
                    TypeReference.create(Address.class),
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class)
            }));
            event.setCtAddress(Keys.toChecksumAddress(paramsA.get(0).getValue().toString()));
            event.setMarketCreator(Keys.toChecksumAddress(paramsA.get(1).getValue().toString()));
            event.setInitialDeposit(Convert.fromWei(paramsA.get(2).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));

            // parse log b
            String dataB = logB.getData();
            List<Type> paramsB =  FunctionReturnDecoder.decode(dataB, Arrays.asList(new TypeReference[]{
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Uint256.class)
            }));
            event.setOwner(Keys.toChecksumAddress(paramsB.get(0).getValue().toString()));
            event.setSutRemain(Convert.fromWei(paramsB.get(1).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            event.setEthRemain(Convert.fromWei(paramsB.get(2).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));

        } catch (Exception e) {
            return null;
        }
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
