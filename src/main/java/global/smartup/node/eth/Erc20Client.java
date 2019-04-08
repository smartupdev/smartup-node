package global.smartup.node.eth;

import global.smartup.node.Config;
import global.smartup.node.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Component
public class Erc20Client {

    private static final Logger log = LoggerFactory.getLogger(Erc20Client.class);

    private static HttpService httpService;

    private static Web3j web3j;

    public static final BigInteger GasPrice = Convert.toWei(BigDecimal.valueOf(10), Convert.Unit.GWEI).toBigInteger();

    public static final BigInteger GasLimit = BigInteger.valueOf(1000_000);

    @Autowired
    private Config config;

    @PostConstruct
    public void init() {
        String address = config.ethProtocol + "://" + config.ethDomain + ":" + config.ethPort + "/";
        httpService = new HttpService(address);
        web3j = Web3j.build(httpService);
    }

    public String getName(String contract) {
        String ret = null;
        try {
            Function fn = new Function(
                    "name", emptyList(), Arrays.asList(new TypeReference<Utf8String>() {})
            );
            String data = FunctionEncoder.encode(fn);
            Map<String, String> map = new HashMap<>();
            map.put("to", contract);
            map.put("data", data);
            Object[] params = new Object[]{map, "latest"};
            Request<String, EthCall> request = new Request("eth_call", Arrays.asList(params), httpService, EthCall.class);
            EthCall resp = request.send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return ret;
            }
            List<Type> list = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            for (Type type : list) {
                ret = type.getValue().toString();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    public String getSymbol(String contract) {
        String ret = null;
        try {
            Function fn = new Function(
                    "symbol", emptyList(), Arrays.asList(new TypeReference<Utf8String>() {})
            );
            String data = FunctionEncoder.encode(fn);
            Map<String, String> map = new HashMap<>();
            map.put("to", contract);
            map.put("data", data);
            Object[] params = new Object[]{map, "latest"};
            Request<String, EthCall> request = new Request("eth_call", Arrays.asList(params), httpService, EthCall.class);
            EthCall resp = request.send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return ret;
            }
            List<Type> list = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            for (Type type : list) {
                ret = type.getValue().toString();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    public Integer getDecimals(String contract) {
        Integer ret = null;
        try {
            Function fn = new Function(
                    "decimals", emptyList(), Arrays.asList(new TypeReference<Uint>() {})
            );
            String data = FunctionEncoder.encode(fn);
            Map<String, String> map = new HashMap<>();
            map.put("to", contract);
            map.put("data", data);
            Object[] params = new Object[]{map, "latest"};
            Request<String, EthCall> request = new Request("eth_call", Arrays.asList(params), httpService, EthCall.class);
            EthCall resp = request.send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return ret;
            }
            List<Type> list = FunctionReturnDecoder.decode(resp.getValue(), fn.getOutputParameters());
            for (Type type : list) {
                ret = Integer.valueOf(type.getValue().toString());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    public BigDecimal getBalance(String contract, String publicKey,  Integer decimals) {
        BigDecimal ret = null;
        try {
            Function fn = new Function(
                    "balanceOf",
                    Arrays.asList(new Address(publicKey)),
                    emptyList()
            );
            String data = FunctionEncoder.encode(fn);
            Map<String, String> map = new HashMap<>();
            map.put("to", contract);
            map.put("data", data);
            Object[] params = new Object[]{map, "latest"};
            Request<String, EthCall> request = new Request("eth_call", Arrays.asList(params), httpService, EthCall.class);
            EthCall resp = request.send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return ret;
            }
            BigInteger balance = Numeric.decodeQuantity(resp.getValue());
            ret = new BigDecimal(balance.toString()).divide(BigDecimal.TEN.pow(decimals));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    public BigInteger getTransactionCount(String publicKey) {
        BigInteger ret = null;
        try {
            EthGetTransactionCount resp = web3j.ethGetTransactionCount(publicKey, DefaultBlockParameterName.LATEST).send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return ret;
            }
            ret = resp.getTransactionCount();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    public String sendTransfer(String contract, BigInteger nonce, String privateKey, String to, BigDecimal value, Integer decimals) {
        String txHash = null;
        try {
            String publicKey = Common.getPublicKeyInHex(privateKey);
            if (nonce == null) {
                nonce = getTransactionCount(publicKey);
            }
            log.info("[ERC20 transfer] contract = {}", contract);
            log.info("[ERC20 transfer] from = {}", publicKey);
            log.info("[ERC20 transfer] to = {}", to);
            log.info("[ERC20 transfer] value = {}", value.toPlainString());
            log.info("[ERC20 transfer] nonce = {}", nonce.toString());

            value = value.multiply(BigDecimal.TEN.pow(decimals));
            Function fn = new Function("transfer",
                    Arrays.asList(new Address(to), new Uint256(value.toBigInteger())),
                    Arrays.asList(new TypeReference<Utf8String>() {}));
            String data = FunctionEncoder.encode(fn);
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    GasPrice,
                    GasLimit,
                    contract,
                    data);

            Credentials credentials = Credentials.create(privateKey);
            byte[] signData =  TransactionEncoder.signMessage(rawTransaction, credentials);
            EthSendTransaction resp = web3j.ethSendRawTransaction(Numeric.toHexString(signData)).send();
            if (resp.hasError()) {
                log.error(resp.getError().getMessage());
                return txHash;
            }
            txHash = resp.getTransactionHash();
            log.info("[ERC20 transfer] txHash = {}", txHash);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return txHash;
    }


}
