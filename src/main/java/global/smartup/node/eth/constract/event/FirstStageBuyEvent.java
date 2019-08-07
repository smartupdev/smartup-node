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
import java.util.Optional;

public class FirstStageBuyEvent {

    private String marketAddress;

    private String userAddress;

    private BigDecimal ctCount;

    private BigDecimal sutCount;


    public static FirstStageBuyEvent parse(TransactionReceipt receipt) {
        FirstStageBuyEvent event = new FirstStageBuyEvent();

        List<Log> list = receipt.getLogs();
        Optional<Log> optional = list.stream()
            .filter(l -> l.getTopics().contains(Const.Exchange.Event.FirstStageBuy)).findFirst();
        if (!optional.isPresent()) {
            return null;
        }

        try {
            Log log = optional.get();
            String data = log.getData();
            List<Type> params =  FunctionReturnDecoder.decode(data, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(Uint256.class)
            }));
            event.setMarketAddress(Keys.toChecksumAddress(params.get(0).getValue().toString()));
            event.setUserAddress(Keys.toChecksumAddress(params.get(1).getValue().toString()));
            event.setCtCount(Convert.fromWei(params.get(2).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            event.setSutCount(Convert.fromWei(params.get(3).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            return event;
        } catch (Exception e) {
            return null;
        }
    }


    public String getMarketAddress() {
        return marketAddress;
    }

    public FirstStageBuyEvent setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
        return this;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public FirstStageBuyEvent setUserAddress(String userAddress) {
        this.userAddress = userAddress;
        return this;
    }

    public BigDecimal getCtCount() {
        return ctCount;
    }

    public FirstStageBuyEvent setCtCount(BigDecimal ctCount) {
        this.ctCount = ctCount;
        return this;
    }

    public BigDecimal getSutCount() {
        return sutCount;
    }

    public FirstStageBuyEvent setSutCount(BigDecimal sutCount) {
        this.sutCount = sutCount;
        return this;
    }
}
