package global.smartup.node.eth;

import com.google.common.base.Preconditions;
import global.smartup.node.util.Checker;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.*;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EthUtil {

    private static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    private static final BigInteger MASK_256 = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE);

    // 撮合交易，基础gas，每10笔，多加
    private static final BigInteger TradeBase = BigInteger.valueOf(20_0000);

    // 撮合交易，递增gas，每1笔，多加
    private static final BigInteger TradeStep = BigInteger.valueOf(10_0000);

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

    public static boolean recoverSignature(String address, String message, String signature) {
        // 注：当客户端为web3js签名的数字类型时，需要先转为十六进制字符串
        try {
            if (!Checker.isAddress(address) || signature.length() != 132) {
                return false;
            }
            byte[] msgHash;
            if (message.startsWith("0x") && message.length() == 66) {
                String prefix = PERSONAL_MESSAGE_PREFIX + "32";
                byte[] mHash = Numeric.hexStringToByteArray(message);
                msgHash = soliditySha3(prefix.getBytes(StandardCharsets.UTF_8), mHash);
            } else {
                String prefix = PERSONAL_MESSAGE_PREFIX + message.length();
                msgHash = Hash.sha3((prefix + message).getBytes());
            }
            Sign.SignatureData sd = EthUtil.getSignData(signature);
            for (int i = 0; i < 4; i++) {
                BigInteger k = Sign.recoverFromSignature((byte) i, new ECDSASignature(new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())), msgHash);
                if (k != null) {
                    String pk = "0x" + Keys.getAddress(k);
                    if (address.equalsIgnoreCase(pk)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkCrateMarketSign(
        String userAddress, BigDecimal sut, String marketId, String marketSymbol, BigDecimal ctCount,
        BigDecimal ctPrice, BigDecimal ctRecyclePrice, BigDecimal fee, long closingTime, String sign)
    {
        BigInteger sutWei = Convert.toWei(sut, Convert.Unit.ETHER).toBigInteger();
        BigInteger ctCountWei = Convert.toWei(ctCount, Convert.Unit.ETHER).toBigInteger();
        BigInteger ctPriceWei = Convert.toWei(ctPrice, Convert.Unit.ETHER).toBigInteger();
        BigInteger ctRecyclePriceWei = Convert.toWei(ctRecyclePrice, Convert.Unit.ETHER).toBigInteger();
        BigInteger feeWei = Convert.toWei(fee, Convert.Unit.ETHER).toBigInteger();
        BigInteger time = BigInteger.valueOf(closingTime);
        byte[] sha3 = soliditySha3(new Address(userAddress), new Uint256(sutWei), new Utf8String(marketId), new Utf8String(marketSymbol),
            new Uint256(ctCountWei), new Uint256(ctPriceWei), new Uint256(ctRecyclePriceWei), new Uint256(feeWei), new Uint256(time));
        String strSha3 = Numeric.toHexString(sha3);
        return recoverSignature(userAddress, strSha3, sign);
    }

    public static boolean checkFistStageBuySign(String userAddress, String marketAddress, BigDecimal ctCount, BigDecimal fee, String timestamp, String sign) {
        BigInteger ctCountWei = Convert.toWei(ctCount, Convert.Unit.ETHER).toBigInteger();
        BigInteger feeWei = Convert.toWei(fee, Convert.Unit.ETHER).toBigInteger();
        byte[] timeHash = Hash.sha3(timestamp.getBytes(StandardCharsets.UTF_8));
        byte[] sha3 = soliditySha3(new Address(marketAddress), ctCountWei, new Address(userAddress), feeWei, timeHash);
        String strSha3 = Numeric.toHexString(sha3);
        return recoverSignature(userAddress, strSha3, sign);
    }

    public static boolean checkBuyMakeSign(String userAddress, String marketAddress, String sutAddress,
                                           BigDecimal volume, BigDecimal price, Long timestamp, String sign) {
        return checkMakeSign(price, volume, timestamp, sutAddress, marketAddress, userAddress, sign);
    }

    public static boolean checkSellMakeSign(String userAddress, String marketAddress, String sutAddress,
                                            BigDecimal volume, BigDecimal price, Long timestamp, String sign) {
        return checkMakeSign(price, volume, timestamp, marketAddress, sutAddress, userAddress, sign);
    }

    private static boolean checkMakeSign(BigDecimal price, BigDecimal volume, Long timestamp,
                                        String sourceAddress, String targetAddress, String userAddress, String sign) {
        BigInteger priceWei = Convert.toWei(price, Convert.Unit.ETHER).toBigInteger();
        BigInteger volumeWei = Convert.toWei(volume, Convert.Unit.ETHER).toBigInteger();
        BigInteger time = BigInteger.valueOf(timestamp);
        byte[] sha3 = soliditySha3(new Uint256(volumeWei), new Uint256(priceWei), new Uint256(time),
            new Address(sourceAddress), new Address(targetAddress), new Address(userAddress));
        String strSha3 = Numeric.toHexString(sha3);
        return recoverSignature(userAddress, strSha3, sign);
    }

    public static boolean checkBuyTakeSign(BigDecimal price, BigDecimal volume, Long timestamp, BigDecimal fee,
                                           String marketAddress, String sutAddress, String userAddress, String sign) {
        return checkTakeSign(price, volume, timestamp, fee, sutAddress, marketAddress, userAddress, sign);
    }

    public static boolean checkSellTakeSign(BigDecimal price, BigDecimal volume, Long timestamp, BigDecimal fee,
                                           String marketAddress, String sutAddress, String userAddress, String sign) {
        return checkTakeSign(price, volume, timestamp, fee, marketAddress, sutAddress, userAddress, sign);
    }

    private static boolean checkTakeSign(BigDecimal price, BigDecimal volume, Long timestamp, BigDecimal fee,
                                        String sourceAddress, String targetAddress, String userAddress, String sign) {
        BigInteger priceWei = Convert.toWei(price, Convert.Unit.ETHER).toBigInteger();
        BigInteger volumeWei = Convert.toWei(volume, Convert.Unit.ETHER).toBigInteger();
        BigInteger time = BigInteger.valueOf(timestamp);
        BigInteger feeWei = Convert.toWei(fee, Convert.Unit.ETHER).toBigInteger();
        byte[] sha3 = soliditySha3(new Uint256(volumeWei), new Uint256(priceWei), new Uint256(time), new Uint256(feeWei),
            new Address(sourceAddress), new Address(targetAddress), new Address(userAddress));
        String strSha3 = Numeric.toHexString(sha3);
        return recoverSignature(userAddress, strSha3, sign);
    }

    public static String sign(String privateKey, String message, boolean needHash) {
        ECKeyPair pair = ECKeyPair.create(new BigInteger(privateKey, 16));

        String signMessage;
        byte[] msgByte = null;
        if (!needHash) {
            String prefix = PERSONAL_MESSAGE_PREFIX + "32";
            byte[] mHash = Numeric.hexStringToByteArray(message);
            msgByte = soliditySha3(prefix.getBytes(), mHash);
        } else {
            signMessage = PERSONAL_MESSAGE_PREFIX + message.length() + message;
            msgByte = signMessage.getBytes();
        }
        Sign.SignatureData data = Sign.signMessage(msgByte, pair, needHash);
        ByteBuffer byteBuffer = ByteBuffer.allocate(65);
        byteBuffer.put(data.getR(), 0, data.getR().length);
        byteBuffer.put(data.getS(), 0, data.getS().length);
        byteBuffer.put(data.getV(), 0, data.getV().length);
        return Numeric.toHexString(byteBuffer.array());
    }

    public static byte[] soliditySha3(Object... data) {
        if (data.length == 1) {
            return Hash.sha3(toBytes(data[0]));
        }
        List<byte[]> arrays = Stream.of(data).map(EthUtil::toBytes).collect(Collectors.toList());
        ByteBuffer buffer = ByteBuffer.allocate(arrays.stream().mapToInt(a -> a.length).sum());

        for (byte[] a : arrays) {
            buffer.put(a);
        }
        byte[] array = buffer.array();
        assert buffer.position() == array.length;
        return Hash.sha3(array);
    }

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

    private static byte[] toBytes(Object obj) {
        if (obj instanceof byte[]) {
            int length = ((byte[]) obj).length;
            Preconditions.checkArgument(length <= 32);
            if (length < 32) {
                return Arrays.copyOf((byte[]) obj, length);
            }
            return (byte[]) obj;
        } else if (obj instanceof BigInteger) {
            BigInteger value = (BigInteger) obj;
            if (value.signum() < 0) {
                value = MASK_256.and(value);
            }
            return Numeric.toBytesPadded(value, 32);
        } else if (obj instanceof Address) {
            return Numeric.toBytesPadded(Numeric.toBigInt(((Address) obj).getValue()), 20);
        } else if (obj instanceof Uint256) {
            Uint uint = (Uint) obj;
            return Numeric.toBytesPadded(uint.getValue(), 32);
        } else if (obj instanceof Uint64) {
            Uint uint = (Uint) obj;
            return Numeric.toBytesPadded(uint.getValue(), 8);
        } else if (obj instanceof Number) {
            long l = ((Number) obj).longValue();
            return toBytes(BigInteger.valueOf(l));
        } else if (obj instanceof Bytes32) {
            return ((Bytes32) obj).getValue();
        } else if (obj instanceof Utf8String) {
            return ((Utf8String) obj).getValue().getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException(obj.getClass().getName());
    }

}
