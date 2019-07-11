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

public class AdminWithdrawEvent {


    private String withdrawer;

    private String token;

    private String owner;

    // 取钱金额
    private BigDecimal value;

    // 手续费
    private BigDecimal fee;

    // 取后余额
    private BigDecimal reamain;


    public String getWithdrawer() {
        return withdrawer;
    }

    public void setWithdrawer(String withdrawer) {
        this.withdrawer = withdrawer;
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

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getReamain() {
        return reamain;
    }

    public void setReamain(BigDecimal reamain) {
        this.reamain = reamain;
    }

    public static AdminWithdrawEvent parse(TransactionReceipt receipt) {
        AdminWithdrawEvent event = new AdminWithdrawEvent();

        List<Log> list = receipt.getLogs();
        Optional<Log> optional = list.stream()
                .filter(l -> l.getTopics().contains(Const.Exchange.Event.AdminWithdrawSign)).findFirst();
        if (!optional.isPresent()) {
            return null;
        }

        try {
            Log log = optional.get();
            String data = log.getData();
            List<Type> params =  FunctionReturnDecoder.decode(data, Arrays.asList(new TypeReference[]{
                    TypeReference.create(Address.class),
                    TypeReference.create(Address.class),
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Uint256.class)
            }));
            event.setWithdrawer(Keys.toChecksumAddress(params.get(0).getValue().toString()));
            event.setToken(Keys.toChecksumAddress(params.get(1).getValue().toString()));
            event.setOwner(Keys.toChecksumAddress(params.get(2).getValue().toString()));
            event.setValue(Convert.fromWei(params.get(3).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            event.setFee(Convert.fromWei(params.get(4).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            event.setReamain(Convert.fromWei(params.get(5).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            return event;
        } catch (Exception e) {
            return null;
        }
    }

}
