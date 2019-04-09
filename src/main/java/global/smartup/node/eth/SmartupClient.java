package global.smartup.node.eth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.Arrays;
import java.util.List;

@Component
public class SmartupClient {

    private static final Logger log = LoggerFactory.getLogger(SmartupClient.class);

    @Autowired
    private EthClient ethClient;


    public TransactionReceipt queryReceipt(String txHash) {
        return ethClient.getTxReceipt(txHash);
    }

    public boolean isTxFail(TransactionReceipt receipt) {
        if (receipt != null) {
            if (receipt.getStatus().equals("0x0")) {
                return true;
            }
        }
        return false;
    }

    public String getCtMarketAddress(TransactionReceipt receipt) {
        try {
            if (receipt == null) {
                return null;
            }
            String status = receipt.getStatus();
            if (status.equals("0x0")) {
                return null;
            }
            List<Log> list = receipt.getLogs();
            if (list.size() != 2) {
                return null;
            }
            Log log = list.get(1);
            String data = log.getData();
            List<Type> params =  FunctionReturnDecoder.decode(data, Arrays.asList(new TypeReference[]{
                    TypeReference.create(Address.class),
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class)
            }));
            if (params.size() != 3) {
                return null;
            }
            return params.get(0).getValue().toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}

