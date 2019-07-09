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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class AdminWithdrawFunc {

    private String token;

    private BigDecimal amount;

    private String owner;

    private BigInteger nonce;

    private BigDecimal feeWithdraw;

    private String sign;


    public static AdminWithdrawFunc parse(Transaction tx) {
        AdminWithdrawFunc func = new AdminWithdrawFunc();
        String input = tx.getInput();

        String str = input.substring(10);
        List<Type> params =  FunctionReturnDecoder.decode(str, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(DynamicBytes.class)
        }));
        func.setToken(Keys.toChecksumAddress(params.get(0).getValue().toString()));
        func.setAmount(Convert.fromWei(params.get(1).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
        func.setOwner(Keys.toChecksumAddress(params.get(2).getValue().toString()));
        func.setNonce(new BigInteger(params.get(3).getValue().toString()));
        func.setFeeWithdraw(Convert.fromWei(params.get(4).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
        func.setSign(params.get(5).getValue().toString());
        return func;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public BigDecimal getFeeWithdraw() {
        return feeWithdraw;
    }

    public void setFeeWithdraw(BigDecimal feeWithdraw) {
        this.feeWithdraw = feeWithdraw;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
