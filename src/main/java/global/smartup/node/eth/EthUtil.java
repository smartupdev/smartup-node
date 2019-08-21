package global.smartup.node.eth;

import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.util.Arrays;

public class EthUtil {

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
}
