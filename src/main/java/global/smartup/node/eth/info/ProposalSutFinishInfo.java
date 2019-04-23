package global.smartup.node.eth.info;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProposalSutFinishInfo {

    public Boolean evenIsAgree;

    public void parseTransactionReceipt(TransactionReceipt receipt) {
        if (receipt == null) {
            return;
        }
        String status = receipt.getStatus();
        if (status.equals("0x0")) {
            return;
        }
        List<Log> list = receipt.getLogs();

        Optional<Log> optional = list.stream()
                .filter(l -> l.getTopics().contains(Constant.CTEventSign.SutProposalFinish))
                .findFirst();
        if (!optional.isPresent()) {
            return;
        }

        Log log = optional.get();
        String data = log.getData();
        List<Type> params =  FunctionReturnDecoder.decode(data, Arrays.asList(new TypeReference[]{
                TypeReference.create(Bool.class),
        }));
        evenIsAgree = (Boolean) params.get(0).getValue();
    }


    public Boolean getEvenIsAgree() {
        return evenIsAgree;
    }

    public void setEvenIsAgree(Boolean evenIsAgree) {
        this.evenIsAgree = evenIsAgree;
    }
}
