package global.smartup.node.eth.constract.func;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.StaticArray3;
import org.web3j.abi.datatypes.generated.StaticArray4;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.Arrays;
import java.util.List;

public class TradeFunc {



    public static TradeFunc parse(Transaction tx) {
        TradeFunc func = new TradeFunc();
        String input = tx.getInput();

        try {
            String str = input.substring(10);
            List<Type> params =  FunctionReturnDecoder.decode(str, Arrays.asList(new TypeReference[]{
                TypeReference.create(DynamicArray.class),
                TypeReference.create(DynamicArray.class),
                TypeReference.create(StaticArray4.class),
                TypeReference.create(StaticArray3.class),
                TypeReference.create(DynamicArray.class),
                TypeReference.create(DynamicArray.class),
                TypeReference.create(DynamicBytes.class)
            }));

            params.get(0).getValue();

            return func;
        } catch (Exception e) {
            return null;
        }
    }
}
