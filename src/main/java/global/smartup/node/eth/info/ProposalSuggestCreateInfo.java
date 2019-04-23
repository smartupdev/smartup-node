package global.smartup.node.eth.info;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProposalSuggestCreateInfo {

    public String eventProposalId;

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
                .filter(l -> l.getTopics().contains(Constant.CTEventSign.SuggestProposalFinish))
                .findFirst();
        if (!optional.isPresent()) {
            return;
        }

        Log log = optional.get();
        String data = log.getData();
        List<Type> params =  FunctionReturnDecoder.decode(data, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Address.class),
                TypeReference.create(Bytes32.class),
        }));

        byte[] bytes = (byte[]) params.get(2).getValue();
        BigInteger i = Numeric.toBigInt(bytes);
        String id = "0x" + i.toString(16);

        this.eventProposalId = id;
    }


    public String getEventProposalId() {
        return eventProposalId;
    }

    public void setEventProposalId(String eventProposalId) {
        this.eventProposalId = eventProposalId;
    }

}
