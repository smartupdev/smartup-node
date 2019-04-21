package global.smartup.node.eth.info;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class CreateProposalSutInfo {

    private String input;

    private String txHash;

    private BigDecimal inputSutAmount;

    public void parseTransaction(Transaction tx) {
        if (tx == null) {
            return;
        }
        this.txHash = tx.getHash();
        this.input = tx.getInput();

        String temp = input.substring(10, input.length());
        List<Type> params =  FunctionReturnDecoder.decode(temp, Arrays.asList(new TypeReference[]{
                TypeReference.create(Uint256.class),
        }));
        this.inputSutAmount = Convert.fromWei(params.get(0).getValue().toString(), Convert.Unit.ETHER);
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

    public BigDecimal getInputSutAmount() {
        return inputSutAmount;
    }

    public void setInputSutAmount(BigDecimal inputSutAmount) {
        this.inputSutAmount = inputSutAmount;
    }
}
