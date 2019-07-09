package global.smartup.node.eth;

import global.smartup.node.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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


}
