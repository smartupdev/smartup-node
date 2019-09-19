package global.smartup.node.eth;

import global.smartup.node.Config;
import global.smartup.node.po.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.*;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

@Component
public class ExchangeClient {

    private static final Logger log = LoggerFactory.getLogger(ExchangeClient.class);

    private static final String EthPlaceHolder = "0x0000000000000000000000000000000000000000";

    @Autowired
    private EthClient ethClient;

    @Autowired
    private Config config;

    public BigDecimal querySutBalance(String address) {
        Function fn = new Function("tokenBalance",
                Arrays.asList(
                        new Address(config.ethSutContract),
                        new Address(address)
                ),
                Arrays.asList(TypeReference.create(Uint256.class)));
        Map<String, String> map = new HashMap<>();
        map.put("to", config.ethExchangeContract);
        map.put("data", FunctionEncoder.encode(fn));
        Object[] params = new Object[]{map, "latest"};

        BigDecimal balance = null;
        try {
            Request<String, EthCall> request = new Request("eth_call", Arrays.asList(params), ethClient.httpService, EthCall.class);
            EthCall resp = request.send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return null;
            }
            BigInteger val = Numeric.decodeQuantity(resp.getValue());
            balance = new BigDecimal(val.toString()).divide(Convert.Unit.ETHER.getWeiFactor(), 20, BigDecimal.ROUND_DOWN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return balance;
    }

    public BigDecimal queryEthBalance(String address) {
        Function fn = new Function("tokenBalance",
                Arrays.asList(
                        new Address(EthPlaceHolder),
                        new Address(address)
                ),
                Arrays.asList(TypeReference.create(Uint256.class)));
        Map<String, String> map = new HashMap<>();
        map.put("to", config.ethExchangeContract);
        map.put("data", FunctionEncoder.encode(fn));
        Object[] params = new Object[]{map, "latest"};

        BigDecimal balance = null;
        try {
            Request<String, EthCall> request = new Request("eth_call", Arrays.asList(params), ethClient.httpService, EthCall.class);
            EthCall resp = request.send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return null;
            }
            BigInteger val = Numeric.decodeQuantity(resp.getValue());
            balance = new BigDecimal(val.toString()).divide(Convert.Unit.ETHER.getWeiFactor(), 20, BigDecimal.ROUND_DOWN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return balance;
    }

    public BigDecimal queryCtRest(String address) {
        Function fn = new Function("tokenBalance",
            Arrays.asList(
                new Address(address),
                new Address(address)
            ),
            Arrays.asList(TypeReference.create(Uint256.class)));
        Map<String, String> map = new HashMap<>();
        map.put("to", config.ethExchangeContract);
        map.put("data", FunctionEncoder.encode(fn));
        Object[] params = new Object[]{map, "latest"};

        BigDecimal balance = null;
        try {
            Request<String, EthCall> request = new Request("eth_call", Arrays.asList(params), ethClient.httpService, EthCall.class);
            EthCall resp = request.send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return null;
            }
            BigInteger val = Numeric.decodeQuantity(resp.getValue());
            balance = new BigDecimal(val.toString()).divide(Convert.Unit.ETHER.getWeiFactor(), 20, BigDecimal.ROUND_DOWN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return balance;
    }

    public String withdrawSut(String userAddress, BigDecimal sut, BigDecimal fee, BigInteger nonce, String sign) {
        String sutAddress = "";

        // tx hash
        return "";
    }

    public String withdrawEth(String userAddress, BigDecimal eth, BigDecimal fee, BigInteger nonce, String sign) {
        String txHash = null;
        Function fn = new Function("adminWithdraw",
                Arrays.asList(
                        // new Address(EthPlaceHolder),
                        // new Uint256(eth),
                        // new Address(userAddress),
                        // new Uint256(nonce),
                        // new Uint256(fee),
                        // new DynamicBytes(sign)
                ),
                Arrays.asList(TypeReference.create(Utf8String.class)));

        String data = FunctionEncoder.encode(fn);

        RawTransaction rawTx = RawTransaction.createTransaction(
                nonce, ethClient.GasPrice, ethClient.GasLimit,
                userAddress, BigInteger.ZERO, data
        );

        Credentials credentials = Credentials.create(config.ethAdminPrivateKey);
        byte[] message = TransactionEncoder.signMessage(rawTx, credentials);
        EthSendTransaction resp = null;
        try {
            resp = ethClient.web3j.ethSendRawTransaction(Numeric.toHexString(message)).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resp != null) {
            if (!resp.hasError()) {
                txHash = resp.getTransactionHash();
                log.info("[ETH withdraw success] txHash = {}", txHash);
            } else {
                log.error("[ETH withdraw error] code = {}, msg = {}", resp.getError().getCode(), resp.getError().getMessage());
            }
        } else {
            log.error("[ETH withdraw error] no response");
        }
        return txHash;
    }

    public String createMarket(String userAddress, BigDecimal sut, String marketId, String symbol, BigDecimal ctCount,
                               BigDecimal ctPrice, BigDecimal ctRecyclePrice, Long closingTime, BigInteger gasLimit, BigInteger gasPrice,
                               String sign) {
        String txHash = null;

        BigInteger nonce = ethClient.getTransactionCount(config.ethAdminPublicKey);
        if (nonce == null) {
            return null;
        }

        BigInteger _sut = Convert.toWei(sut, Convert.Unit.ETHER).toBigInteger();
        BigInteger _ctCount = Convert.toWei(ctCount, Convert.Unit.ETHER).toBigInteger();
        BigInteger _ctPrice = Convert.toWei(ctPrice, Convert.Unit.ETHER).toBigInteger();
        BigInteger _ctRecyclePrice = Convert.toWei(ctRecyclePrice, Convert.Unit.ETHER).toBigInteger();
        BigInteger _gasPrice = Convert.toWei(new BigDecimal(gasPrice), Convert.Unit.GWEI).toBigInteger();
        BigInteger _gasFee = gasLimit.multiply(_gasPrice);

        Function fn = new Function(
                "createCtMarket",
                Arrays.asList(
                        new Address(userAddress),
                        new Uint256(_sut),
                        new Utf8String(marketId),
                        new Utf8String(symbol),
                        new Uint256(_ctCount),
                        new Uint256(_ctPrice),
                        new Uint256(_ctRecyclePrice),
                        new Uint256(_gasFee),
                        new Uint256(closingTime),
                        new DynamicBytes(Numeric.hexStringToByteArray(sign))
                ),
                emptyList()
        );

        String data = FunctionEncoder.encode(fn);
        RawTransaction rawTx = RawTransaction.createTransaction(nonce, _gasPrice, gasLimit, config.ethExchangeContract, BigInteger.ZERO, data);
        Credentials credentials = Credentials.create(config.ethAdminPrivateKey);
        byte[] message = TransactionEncoder.signMessage(rawTx, credentials);

        EthSendTransaction resp = null;
        try {
            resp = ethClient.web3j.ethSendRawTransaction(Numeric.toHexString(message)).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resp != null) {
            if (!resp.hasError()) {
                txHash = resp.getTransactionHash();
                log.info("[Create Market success] txHash = {}", txHash);
            } else {
                log.error("[Create Market error] code = {}, msg = {}", resp.getError().getCode(), resp.getError().getMessage());
            }
        } else {
            log.error("[Create Market error] no response");
        }

        return txHash;
    }

    public String firstStageBuy(String userAddress, String marketAddress, BigDecimal ctCount,
                                BigInteger gasLimit, BigInteger gasPrice, String timestamp, String sign) {
        String txHash = null;

        BigInteger nonce = ethClient.getTransactionCount(config.ethAdminPublicKey);
        if (nonce == null) {
            return null;
        }

        BigInteger _ctCount = Convert.toWei(ctCount, Convert.Unit.ETHER).toBigInteger();
        BigInteger _gasPrice = Convert.toWei(new BigDecimal(gasPrice), Convert.Unit.GWEI).toBigInteger();
        BigInteger _gasFee = gasLimit.multiply(_gasPrice);
        byte[] timeHash = Hash.sha3(timestamp.getBytes(StandardCharsets.UTF_8));

        Function fn = new Function(
            "buyCt",
            Arrays.asList(
                new Address(marketAddress),
                new Uint256(_ctCount),
                new Address(userAddress),
                new Uint256(_gasFee),
                new Bytes32(timeHash),
                new DynamicBytes(Numeric.hexStringToByteArray(sign))
            ),
            emptyList()
        );

        String data = FunctionEncoder.encode(fn);
        RawTransaction rawTx = RawTransaction.createTransaction(nonce, _gasPrice, gasLimit, config.ethExchangeContract, BigInteger.ZERO, data);
        Credentials credentials = Credentials.create(config.ethAdminPrivateKey);
        byte[] message = TransactionEncoder.signMessage(rawTx, credentials);

        EthSendTransaction resp = null;
        try {
            resp = ethClient.web3j.ethSendRawTransaction(Numeric.toHexString(message)).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resp != null) {
            if (!resp.hasError()) {
                txHash = resp.getTransactionHash();
                log.info("[First stage buy success] txHash = {}", txHash);
            } else {
                log.error("[First stage buy error] code = {}, msg = {}", resp.getError().getCode(), resp.getError().getMessage());
            }
        } else {
            log.error("[First stage buy error] no response");
        }

        return txHash;
    }

    /**
     * @param isTakeBuy 吃买单，或吃卖单
     * @param makeOrders
     *      price , volume, timestamp, maker, sign
     * @return
     */
    public String take(boolean isTakeBuy, String sutAddress, String marketAddress,
                       BigDecimal price, BigDecimal volume, Long timestamp, BigInteger gasPrice,BigInteger gasLimit,
                       String taker, String takeSign, List<Map<String, Object>> makeOrders) {
        String txHash = null;
        BigDecimal fee = Convert.fromWei(new BigDecimal(gasLimit.multiply(gasPrice)), Convert.Unit.GWEI);

        List<Uint256> makerValue = new ArrayList<>();
        List<Address> makeAddress = new ArrayList<>();
        List<Uint256> takeValue = new ArrayList<>();
        List<Address> takeAddress = new ArrayList<>();
        List<Bytes32> signRS = new ArrayList<>();
        List<Uint8> signV = new ArrayList<>();

        takeValue.add(new Uint256(Convert.fromWei(volume, Convert.Unit.ETHER).toBigInteger()));
        takeValue.add(new Uint256(Convert.fromWei(price, Convert.Unit.ETHER).toBigInteger()));
        takeValue.add(new Uint256(timestamp));
        takeValue.add(new Uint256(Convert.fromWei(fee, Convert.Unit.ETHER).toBigInteger()));
        // 1.sell address
        // 2.buy address
        if (isTakeBuy) {
            takeAddress.add(new Address(marketAddress));
            takeAddress.add(new Address(sutAddress));
        } else {
            takeAddress.add(new Address(sutAddress));
            takeAddress.add(new Address(marketAddress));
        }
        takeAddress.add(new Address(taker));

        for (Map<String, Object> map : makeOrders) {
            BigDecimal p = (BigDecimal) map.get("price");
            BigDecimal v = (BigDecimal) map.get("volume");
            Long t = (Long) map.get("timestamp");
            String maker = (String) map.get("maker");
            String sign = (String) map.get("sign");

            makerValue.add(new Uint256(Convert.fromWei(v, Convert.Unit.ETHER).toBigInteger()));
            makerValue.add(new Uint256(Convert.fromWei(p, Convert.Unit.ETHER).toBigInteger()));
            makerValue.add(new Uint256(t));

            // 1.sell address
            // 2.buy address
            if (isTakeBuy) {
                makeAddress.add(new Address(sutAddress));
                makeAddress.add(new Address(marketAddress));
            } else {
                makeAddress.add(new Address(marketAddress));
                makeAddress.add(new Address(sutAddress));
            }
            makeAddress.add(new Address(maker));

            Tuple3<Bytes32, Bytes32, Uint8> data = getSignData(sign);
            signRS.add(data.component1());
            signRS.add(data.component2());
            signV.add(data.component3());
        }

        BigInteger nonce = ethClient.getTransactionCount(config.ethAdminPublicKey);
        if (nonce == null) {
            return null;
        }

        Function fn = new Function(
            "trade",
            Arrays.asList(
                new DynamicArray(Uint256.class, makerValue),
                new DynamicArray(Address.class, makeAddress),
                new StaticArray4(Uint256.class, takeValue),
                new StaticArray3(Address.class, takeAddress),
                new DynamicArray(Bytes32.class, signRS),
                new DynamicArray(Uint8.class, signV),
                new DynamicBytes(takeSign.getBytes())
            ),
            emptyList()
        );

        String data = FunctionEncoder.encode(fn);
        RawTransaction rawTx = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, config.ethExchangeContract, BigInteger.ZERO, data);
        Credentials credentials = Credentials.create(config.ethAdminPrivateKey);
        byte[] message = TransactionEncoder.signMessage(rawTx, credentials);

        EthSendTransaction resp = null;
        try {
            resp = ethClient.web3j.ethSendRawTransaction(Numeric.toHexString(message)).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resp != null) {
            if (!resp.hasError()) {
                txHash = resp.getTransactionHash();
                log.info("[Take success] txHash = {}", txHash);
            } else {
                log.error("[Take error] code = {}, msg = {}", resp.getError().getCode(), resp.getError().getMessage());
            }
        } else {
            log.error("[Take error] no response");
        }

        return txHash;
    }

    private Tuple3<Bytes32, Bytes32, Uint8> getSignData(String sign) {
        byte[] signatureBytes = Numeric.hexStringToByteArray(sign);
        byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }
        return new Tuple3<>(new Bytes32(r), new Bytes32(s), new Uint8(v));
    }

}
