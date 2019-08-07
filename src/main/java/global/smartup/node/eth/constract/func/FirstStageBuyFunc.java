package global.smartup.node.eth.constract.func;

import global.smartup.node.constant.BuConstant;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import sun.nio.cs.ext.DoubleByte;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class FirstStageBuyFunc {

    private String userAddress;

    private String marketAddress;

    private BigDecimal ctCount;

    private BigDecimal fee;

    private String timeHash;

    private String sign;


    public static FirstStageBuyFunc parse(Transaction tx) {
        FirstStageBuyFunc func = new FirstStageBuyFunc();
        String input = tx.getInput();

        try {
            String str = input.substring(10);
            List<Type> params =  FunctionReturnDecoder.decode(str, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(Bytes32.class),
                TypeReference.create(DynamicBytes.class)
            }));
            func.setMarketAddress(Keys.toChecksumAddress(params.get(0).getValue().toString()));
            func.setCtCount(Convert.fromWei(params.get(1).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            func.setUserAddress(Keys.toChecksumAddress(params.get(2).getValue().toString()));
            func.setFee(Convert.fromWei(params.get(3).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            func.setTimeHash(params.get(4).getTypeAsString());
            func.setSign(params.get(5).getTypeAsString());
            return func;
        } catch (Exception e) {
            return null;
        }
    }


    public String getUserAddress() {
        return userAddress;
    }

    public FirstStageBuyFunc setUserAddress(String userAddress) {
        this.userAddress = userAddress;
        return this;
    }

    public String getMarketAddress() {
        return marketAddress;
    }

    public FirstStageBuyFunc setMarketAddress(String marketAddress) {
        this.marketAddress = marketAddress;
        return this;
    }

    public BigDecimal getCtCount() {
        return ctCount;
    }

    public FirstStageBuyFunc setCtCount(BigDecimal ctCount) {
        this.ctCount = ctCount;
        return this;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public FirstStageBuyFunc setFee(BigDecimal fee) {
        this.fee = fee;
        return this;
    }

    public String getTimeHash() {
        return timeHash;
    }

    public FirstStageBuyFunc setTimeHash(String timeHash) {
        this.timeHash = timeHash;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public FirstStageBuyFunc setSign(String sign) {
        this.sign = sign;
        return this;
    }
}
