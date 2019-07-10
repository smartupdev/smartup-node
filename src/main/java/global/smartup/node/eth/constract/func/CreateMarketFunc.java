package global.smartup.node.eth.constract.func;

import global.smartup.node.constant.BuConstant;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class CreateMarketFunc {

    private String creator;

    // 初始押金
    private BigDecimal deposit;

    // 市场名字
    private String marketName;

    // 市场id
    private String marketSymbol;

    // ct发行量
    private BigDecimal supply;

    // 初始兑换率 1个ct可以兑换多少sut 兑换率以wei为单位
    private BigDecimal rate;

    // 回收兑换率
    private BigDecimal lastRate;

    private BigDecimal gasFee;

    private String signature;


    public static CreateMarketFunc parse(Transaction tx) {
        CreateMarketFunc func = new CreateMarketFunc();
        try {
            String input = tx.getInput();
            // String input = "0x0d4f100900000000000000000000000095320bf4e0997743779e5fd7b03454bd9958207b0000000000000000000000000000000000000000000000878678326eac9000000000000000000000000000000000000000000000000000000000000000000120000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000021e19e0c9bab24000000000000000000000000000000000000000000000000000008ac7230489e800000000000000000000000000000000000000000000000000003782dace9d900000000000000000000000000000000000000000000000000000004380663abb800000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000b6166383933637173777a6b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000b6166383933637173777a6b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008430786334653938653066393935323036643239653864366130346637323263633331353234643734613037656637383334636565303663356636613232346135626532653936643036623838633032636533393266373662653538643133616639306362613664303332336235393161316439326364666265643066333233653635316200000000000000000000000000000000000000000000000000000000";
            String str = input.substring(10);
            List<Type> params = FunctionReturnDecoder.decode(str, Arrays.asList(new TypeReference[]{
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Utf8String.class),
                    TypeReference.create(Utf8String.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(DynamicBytes.class)
            }));
            func.setCreator(Keys.toChecksumAddress(params.get(0).getValue().toString()));
            func.setDeposit(Convert.fromWei(params.get(1).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            func.setMarketName(params.get(2).toString());
            func.setMarketSymbol(params.get(3).toString());
            func.setSupply(Convert.fromWei(params.get(4).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            func.setRate(Convert.fromWei(params.get(5).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            func.setLastRate(Convert.fromWei(params.get(6).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
            func.setGasFee(Convert.fromWei(params.get(7).getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale));
        } catch (Exception e) {
            return null;
        }
        return func;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getMarketSymbol() {
        return marketSymbol;
    }

    public void setMarketSymbol(String marketSymbol) {
        this.marketSymbol = marketSymbol;
    }

    public BigDecimal getSupply() {
        return supply;
    }

    public void setSupply(BigDecimal supply) {
        this.supply = supply;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getLastRate() {
        return lastRate;
    }

    public void setLastRate(BigDecimal lastRate) {
        this.lastRate = lastRate;
    }

    public BigDecimal getGasFee() {
        return gasFee;
    }

    public void setGasFee(BigDecimal gasFee) {
        this.gasFee = gasFee;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
