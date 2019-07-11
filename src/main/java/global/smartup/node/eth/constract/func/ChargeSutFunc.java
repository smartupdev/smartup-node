package global.smartup.node.eth.constract.func;

import global.smartup.node.constant.BuConstant;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ChargeSutFunc {

    // exchange合约地址
    private String spender;

    // 存放金额
    private BigDecimal value;

    // 暂无作用，占位
    private String extraData;


    public static ChargeSutFunc parse(Transaction tx) {
        ChargeSutFunc func = new ChargeSutFunc();
        String input = tx.getInput();
        try {
            String str = input.substring(10);
            List<Type> params =  FunctionReturnDecoder.decode(str, Arrays.asList(new TypeReference[]{
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(DynamicBytes.class)
            }));
            func.setSpender(Keys.toChecksumAddress(params.get(0).getValue().toString()));
            func.setValue(Convert.fromWei(params.get(1).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            return func;
        } catch (Exception e) {
            return null;
        }
    }

    public String getSpender() {
        return spender;
    }

    public void setSpender(String spender) {
        this.spender = spender;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
