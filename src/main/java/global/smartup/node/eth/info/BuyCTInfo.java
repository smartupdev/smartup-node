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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BuyCTInfo {

    private String txHash;

    private String input;

    private Date blockTime;



    private String inputMarketAddress;

    private BigDecimal inputSUT;

    private BigDecimal inputCT;


    private String eventMarketAddress;

    private String eventUserAddress;

    private BigDecimal eventSUTOffer;

    private BigDecimal eventSUT;

    private BigDecimal eventCT;


    public static boolean isBuyCTTransaction(String input, String smartupContract) {
        if (input.endsWith("0") &&
                !input.toLowerCase().contains(smartupContract.toLowerCase().substring(2, smartupContract.length()))
                ) {
            return true;
        }
        return false;
    }

    public void parseTransaction(Transaction tx) {
        if (tx == null) {
            return;
        }
        this.input = tx.getInput();
        this.txHash = tx.getHash();
        String temp = input.substring(10, input.length());
        List<Type> params =  FunctionReturnDecoder.decode(temp, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(DynamicBytes.class)
        }));

        this.inputMarketAddress = params.get(0).getValue().toString();
        this.inputSUT = Convert.fromWei(params.get(1).getValue().toString(), Convert.Unit.ETHER);
        DynamicBytes dynamicBytes = (DynamicBytes) params.get(2);
        BigInteger val = new BigInteger(dynamicBytes.getValue());
        this.inputCT = new BigDecimal(val).divide(Convert.Unit.ETHER.getWeiFactor());
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
                TypeReference.create(Uint256.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(Uint256.class)
        }));
        eventMarketAddress = params.get(0).getValue().toString();
        eventUserAddress = params.get(1).getValue().toString();
        eventSUTOffer = Convert.fromWei(params.get(2).getValue().toString(), Convert.Unit.ETHER);
        eventSUT = Convert.fromWei(params.get(3).getValue().toString(), Convert.Unit.ETHER);
        eventCT = Convert.fromWei(params.get(4).getValue().toString(), Convert.Unit.ETHER);
    }




    public Date getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Date blockTime) {
        this.blockTime = blockTime;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getInputMarketAddress() {
        return inputMarketAddress;
    }

    public void setInputMarketAddress(String inputMarketAddress) {
        this.inputMarketAddress = inputMarketAddress;
    }

    public BigDecimal getInputSUT() {
        return inputSUT;
    }

    public void setInputSUT(BigDecimal inputSUT) {
        this.inputSUT = inputSUT;
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

    public BigDecimal getEventSUTOffer() {
        return eventSUTOffer;
    }

    public void setEventSUTOffer(BigDecimal eventSUTOffer) {
        this.eventSUTOffer = eventSUTOffer;
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
