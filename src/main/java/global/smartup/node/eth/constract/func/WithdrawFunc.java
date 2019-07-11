package global.smartup.node.eth.constract.func;

import global.smartup.node.constant.BuConstant;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class WithdrawFunc {

    private String token;

    private BigDecimal amount;


    public static WithdrawFunc parse(Transaction tx) {
        WithdrawFunc func = new WithdrawFunc();
        String input = tx.getInput();

        try {
            String str = input.substring(10);
            List<Type> params =  FunctionReturnDecoder.decode(str, Arrays.asList(new TypeReference[]{
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class)
            }));
            func.setToken(Keys.toChecksumAddress(params.get(0).getValue().toString()));
            func.setAmount(Convert.fromWei(params.get(1).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            return func;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isWithdrawEth() {
        assert token != null;
        if ("0x0000000000000000000000000000000000000000".equals(token)) {
            return true;
        } else {
            return false;
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
