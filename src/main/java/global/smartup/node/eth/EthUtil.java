package global.smartup.node.eth;

import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class EthUtil {

    // 撮合交易，基础gas，每10笔，多加
    public static final BigInteger TradeBase = BigInteger.valueOf(20_0000);

    // 撮合交易，递增gas，每1笔，多加
    public static final BigInteger TradeStep = BigInteger.valueOf(10_0000);

    public static Sign.SignatureData getSignData(String signMessage) {
        byte[] signatureBytes = Numeric.hexStringToByteArray(signMessage);
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }
        return new Sign.SignatureData(
            v,
            Arrays.copyOfRange(signatureBytes, 0, 32),
            Arrays.copyOfRange(signatureBytes, 32, 64));
    }

    public static BigInteger getTradeGasLimit(Integer times) {
        assert times != null;
        if (times.compareTo(0) == 0) {
            return BigInteger.ZERO;
        }
        BigInteger limit = TradeStep.multiply(BigInteger.valueOf(times));
        int base = (times / 10) + (times % 10 == 0 ? 0 : 1);
        limit = limit.add(TradeBase.multiply(BigInteger.valueOf(base)));
        return limit;
    }

}
