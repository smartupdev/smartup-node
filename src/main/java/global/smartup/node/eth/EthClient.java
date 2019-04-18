package global.smartup.node.eth;

import global.smartup.node.Config;
import global.smartup.node.util.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

@Component
public class EthClient {

    private static final Logger log = LoggerFactory.getLogger(EthClient.class);

    public static Web3j web3j;

    public static HttpService httpService;

    public static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    public static final BigInteger GasPrice = Convert.toWei(BigDecimal.valueOf(10), Convert.Unit.GWEI).toBigInteger();

    public static final BigInteger GasLimit = BigInteger.valueOf(1000_000);

    @Autowired
    public Config config;

    @PostConstruct
    public void init() {
        String address = config.ethProtocol + "://" + config.ethDomain + ":" + config.ethPort + "/";
        httpService = new HttpService(address);
        web3j = Web3j.build(httpService);
    }

    public BigInteger getLastBlockNumber() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            if (!blockNumber.hasError()) {
                return blockNumber.getBlockNumber();
            } else {
                log.error("getLastBlockNumber error {}", blockNumber.getError().getMessage());
            }
        } catch (IOException e) {
            log.error("getLastBlockNumber error {}", e.getMessage());
        }
        return null;
    }

    public EthBlock.Block getBlockByNumber(BigInteger number, boolean isDetail) {
        if(number.compareTo(BigInteger.ONE)<0){
            number=BigInteger.ONE;
        }
        DefaultBlockParameter blockNumber = DefaultBlockParameter.valueOf(number);
        try {
            EthBlock ethBlock = web3j.ethGetBlockByNumber(blockNumber, isDetail).send();
            if (!ethBlock.hasError()) {
                return ethBlock.getBlock();
            } else {
                log.error("getBlockByNumber error {}", ethBlock.getError().getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Transaction getTx(String txHash) {
        try {
            EthTransaction transaction = web3j.ethGetTransactionByHash(txHash).send();
            if (transaction == null) {
                log.error("getTx error no response");
                return null;
            }
            if (transaction.hasError()) {
                log.error("getTx error " + transaction.getError().getMessage());
                return null;
            }
            if (!transaction.getTransaction().isPresent()) {
                log.error("getTx error transaction can not present");
                return null;
            }
            return transaction.getTransaction().get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public TransactionReceipt getTxReceipt(String txHash) {
        try {
            EthGetTransactionReceipt receiptResp = web3j.ethGetTransactionReceipt(txHash).send();
            if (receiptResp == null) {
                log.error("getTxReceipt error no response");
                return null;
            }
            if (receiptResp.hasError()) {
                log.error("getTxReceipt error " + receiptResp.getError().getMessage());
                return null;
            }
            if (!receiptResp.getTransactionReceipt().isPresent()) {
                log.error("getTxReceipt error receiptResp can not present");
                return null;
            }
            return receiptResp.getTransactionReceipt().get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public boolean isTransactionFail(TransactionReceipt receipt) {
        String status = receipt.getStatus();
        if (status.equals("0x0")) {
            return true;
        }
        return false;
    }

    public boolean recoverSignature (String address, String message, String signature) {
        try {
            if (!Checker.isAddress(address) || signature.length() != 132) {
                return false;
            }
            String prefix = PERSONAL_MESSAGE_PREFIX + message.length();
            byte[] msgHash = Hash.sha3((prefix + message).getBytes());
            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);

            byte v = signatureBytes[64];
            if (v < 27) {
                v += 27;
            }

            Sign.SignatureData sd = new Sign.SignatureData(
                    v,
                    Arrays.copyOfRange(signatureBytes, 0, 32),
                    Arrays.copyOfRange(signatureBytes, 32, 64));

            for (int i = 0; i < 4; i++) {
                BigInteger k = Sign.recoverFromSignature(
                        (byte) i,
                        new ECDSASignature(new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())),
                        msgHash);
                if (k != null) {
                    String pk =  "0x" + Keys.getAddress(k);
                    if (address.equalsIgnoreCase(pk)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public void test() {

    }

}
