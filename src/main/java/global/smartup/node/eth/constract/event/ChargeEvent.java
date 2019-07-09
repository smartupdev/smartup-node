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

public class ChargeEvent {


    private String token;

    private String owner;

    // 充值金额
    private BigDecimal amount;

    // 冲后余额
    private BigDecimal total;


    public static ChargeEvent parse(TransactionReceipt receipt) {
        ChargeEvent event = new ChargeEvent();

        List<Log> list = receipt.getLogs();
        Optional<Log> optional = list.stream()
                .filter(l -> l.getTopics().contains(Const.Exchange.Event.ChargeSign)).findFirst();
        if (!optional.isPresent()) {
            return null;
        }

        Log log = optional.get();
        String data = log.getData();
        List<Type> params =  FunctionReturnDecoder.decode(data, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(Uint256.class)
        }));
        event.setToken(Keys.toChecksumAddress(params.get(0).getValue().toString()));
        event.setOwner(Keys.toChecksumAddress(params.get(1).getValue().toString()));
        event.setAmount(Convert.fromWei(params.get(2).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
        event.setTotal(Convert.fromWei(params.get(3).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));

        return event;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

}
