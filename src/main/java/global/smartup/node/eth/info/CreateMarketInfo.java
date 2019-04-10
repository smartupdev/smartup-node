package global.smartup.node.eth.info;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class CreateMarketInfo {

    public static final String ByteLastFlag = "01";

    private String txHash;

    private String input;

    private String inputSmartupAddress;

    private BigDecimal inputAmount;


    private String eventMarketAddress;

    private String eventCreatorAddress;

    private BigDecimal eventAmount;


    public void parseTransaction(Transaction tx) {
        if (tx == null) {
            return;
        }
        this.txHash = tx.getHash();
        this.input = tx.getInput();

        if (!input.endsWith(ByteLastFlag)) {
            return;
        }

        String temp = input.substring(10, input.length());
        List<Type> params =  FunctionReturnDecoder.decode(temp, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(DynamicBytes.class)
        }));
        this.inputSmartupAddress = params.get(0).getValue().toString();
        this.inputAmount = Convert.fromWei(params.get(1).getValue().toString(), Convert.Unit.ETHER);
    }

    public void parseTransactionReceipt(TransactionReceipt receipt) {
        if (receipt == null) {
            return;
        }
        String status = receipt.getStatus();
        if (status.equals("0x0")) {
            return;
        }
        List<Log> list = receipt.getLogs();
        if (list.size() != 2) {
            return;
        }
        Log log = list.get(1);
        String data = log.getData();
        List<Type> params =  FunctionReturnDecoder.decode(data, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class)
        }));
        eventMarketAddress = params.get(0).getValue().toString();
        eventCreatorAddress = params.get(1).getValue().toString();
        eventAmount = Convert.fromWei(params.get(2).getValue().toString(), Convert.Unit.ETHER);
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getInputSmartupAddress() {
        return inputSmartupAddress;
    }

    public void setInputSmartupAddress(String inputSmartupAddress) {
        this.inputSmartupAddress = inputSmartupAddress;
    }

    public BigDecimal getInputAmount() {
        return inputAmount;
    }

    public void setInputAmount(BigDecimal inputAmount) {
        this.inputAmount = inputAmount;
    }


    public String getEventMarketAddress() {
        return eventMarketAddress;
    }

    public void setEventMarketAddress(String eventMarketAddress) {
        this.eventMarketAddress = eventMarketAddress;
    }

    public String getEventCreatorAddress() {
        return eventCreatorAddress;
    }

    public void setEventCreatorAddress(String eventCreatorAddress) {
        this.eventCreatorAddress = eventCreatorAddress;
    }

    public BigDecimal getEventAmount() {
        return eventAmount;
    }

    public void setEventAmount(BigDecimal eventAmount) {
        this.eventAmount = eventAmount;
    }
}
