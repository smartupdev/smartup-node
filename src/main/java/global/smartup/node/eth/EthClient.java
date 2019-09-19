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
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Transaction getTx(String txHash) {
        try {
            EthTransaction transaction = web3j.ethGetTransactionByHash(txHash).send();
            if (transaction == null) {
                return null;
            }
            if (transaction.hasError()) {
                return null;
            }
            if (!transaction.getTransaction().isPresent()) {
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

    public boolean isTransactionSuccess(TransactionReceipt receipt) {
        return !isTransactionFail(receipt);
    }

    public BigInteger getTransactionCount(String publicKey) {
        BigInteger ret = null;
        try {
            EthGetTransactionCount resp = web3j.ethGetTransactionCount(publicKey, DefaultBlockParameterName.LATEST).send();
            if (resp != null) {
                if (!resp.hasError()) {
                    ret = resp.getTransactionCount();
                } else {
                    log.error("[ETH getTransactionCount error] code = {}, msg = {}", resp.getError().getCode(), resp.getError().getMessage());
                }
            } else {
                log.error("[ETH getTransactionCount error] server no response");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    public BigDecimal getBalance(String address) {
        BigDecimal ret = null;
        try {
            EthGetBalance resp = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            if (resp != null) {
                if (!resp.hasError()) {
                    BigInteger bi = resp.getBalance();
                    ret = Convert.fromWei(new BigDecimal(bi), Convert.Unit.ETHER);
                } else {
                    log.error("[ETH getBalance error] code = {}, msg = {}", resp.getError().getCode(), resp.getError().getMessage());
                }
            } else {
                log.error("[ETH getBalance error] server no response");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    public BigInteger getNonce(String address) {
        // TODO get Nonce
        return null;
    }

}
