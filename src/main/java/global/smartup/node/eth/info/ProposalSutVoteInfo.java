package global.smartup.node.eth.info;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ProposalSutVoteInfo {

    private String txHash;

    private String input;

    private Date blockTime;


    private Boolean inputVote;


    public void parseTransaction(Transaction tx) {
        if (tx == null) {
            return;
        }
        this.txHash = tx.getHash();
        this.input = tx.getInput();

        String temp = input.substring(10, input.length());
        List<Type> params =  FunctionReturnDecoder.decode(temp, Arrays.asList(new TypeReference[]{
                TypeReference.create(Bool.class),
        }));
        this.inputVote = ((Bool)params.get(0)).getValue();
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

    public Boolean getInputVote() {
        return inputVote;
    }

    public void setInputVote(Boolean inputVote) {
        this.inputVote = inputVote;
    }
}
