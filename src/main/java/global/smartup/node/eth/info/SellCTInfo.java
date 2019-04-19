package global.smartup.node.eth.info;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SellCTInfo {

    private String txHash;

    private String input;

    private Date blockTime;


    private BigDecimal inputCT;


    private String eventMarketAddress;

    private String eventUserAddress;

    private BigDecimal eventSUT;

    private BigDecimal eventCT;



    public void parseTransaction(Transaction tx) {
        if (tx == null) {
            return;
        }
        this.input = tx.getInput();
        this.txHash = tx.getHash();
        String temp = input.substring(10, input.length());
        List<Type> params =  FunctionReturnDecoder.decode(temp, Arrays.asList(new TypeReference[]{
                TypeReference.create(Uint256.class),
        }));
        this.inputCT = Convert.fromWei(params.get(0).getValue().toString(), Convert.Unit.ETHER);
    }


    public void parseTransactionReceipt(TransactionReceipt receipt) {
        if (receipt == null) {
            return;
        }
        String status = receipt.getStatus();
        if ("0x0".equals(status)) {
            return;
        }
        List<Log> list = receipt.getLogs();
        if (list.size() < 2) {
            return;
        }
        Log log = list.get(1);
        String data = log.getData();
        List<Type> params =  FunctionReturnDecoder.decode(data, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(Uint256.class)
        }));
        eventMarketAddress = Keys.toChecksumAddress(params.get(0).getValue().toString());
        eventUserAddress = Keys.toChecksumAddress(params.get(1).getValue().toString());
        eventSUT = Convert.fromWei(params.get(2).getValue().toString(), Convert.Unit.ETHER);
        eventCT = Convert.fromWei(params.get(3).getValue().toString(), Convert.Unit.ETHER);
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

    public Date getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Date blockTime) {
        this.blockTime = blockTime;
    }

    public BigDecimal getInputCT() {
        return inputCT;
    }

    public void setInputCT(BigDecimal inputCT) {
        this.inputCT = inputCT;
    }

    public String getEventMarketAddress() {
        return eventMarketAddress;
    }

    public void setEventMarketAddress(String eventMarketAddress) {
        this.eventMarketAddress = eventMarketAddress;
    }

    public String getEventUserAddress() {
        return eventUserAddress;
    }

    public void setEventUserAddress(String eventUserAddress) {
        this.eventUserAddress = eventUserAddress;
    }

    public BigDecimal getEventSUT() {
        return eventSUT;
    }

    public void setEventSUT(BigDecimal eventSUT) {
        this.eventSUT = eventSUT;
    }

    public BigDecimal getEventCT() {
        return eventCT;
    }

    public void setEventCT(BigDecimal eventCT) {
        this.eventCT = eventCT;
    }
}
