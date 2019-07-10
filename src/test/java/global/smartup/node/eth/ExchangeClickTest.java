package global.smartup.node.eth;

import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static java.util.Collections.emptyList;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExchangeClickTest {

    @Autowired
    private ExchangeClient exchangeClient;

    @Test
    public void querySutBalance() {
        BigDecimal balance = exchangeClient.querySutBalance("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        System.out.println(balance.toPlainString());
    }

    @Test
    public void queryEthBalance() {
        BigDecimal balance = exchangeClient.queryEthBalance("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        System.out.println(balance.toPlainString());
    }

    @Test
    public void encodeFun() {

        String userAddress = "0x95320Bf4E0997743779e5FD7B03454Bd9958207B";
        BigDecimal sut = BigDecimal.valueOf(2500);
        String marketId = "af893cqswzk";
        BigDecimal ctCount = BigDecimal.valueOf(10000);
        BigDecimal ctPrice = BigDecimal.valueOf(10);
        BigDecimal ctRecyclePrice = BigDecimal.valueOf(4);
        BigInteger gasLimit = BigInteger.valueOf(19000000);
        BigInteger gasPrice = BigInteger.valueOf(10);
        String sign = "1d0a2ef1ef7eb3b060aad102bbce8edc3c66131ef05624772ccb9e314fbf61081689f2967ad4d1cb3530e08a9bf0ce2adf2bda521d8068c1a69a8292b6e5074c1b";

        BigInteger _sut = Convert.toWei(sut, Convert.Unit.ETHER).toBigInteger();
        BigInteger _ctCount = Convert.toWei(ctCount, Convert.Unit.ETHER).toBigInteger();
        BigInteger _ctPrice = Convert.toWei(ctPrice, Convert.Unit.ETHER).toBigInteger();
        BigInteger _ctRecyclePrice = Convert.toWei(ctRecyclePrice, Convert.Unit.ETHER).toBigInteger();
        BigInteger _gasPrice = Convert.toWei(new BigDecimal(gasPrice), Convert.Unit.GWEI).toBigInteger();
        BigInteger _gasFee = gasLimit.multiply(_gasPrice);

        Function fn = new Function(
                "createCtMarket",
                Arrays.asList(
                        new Address(userAddress),
                        new Uint256(_sut),
                        new Utf8String(marketId),
                        new Utf8String(marketId),
                        new Uint256(_ctCount),
                        new Uint256(_ctPrice),
                        new Uint256(_ctRecyclePrice),
                        new Uint256(_gasFee),
                        new DynamicBytes(Numeric.hexStringToByteArray(sign))
                ),
                emptyList()
        );

        String data = FunctionEncoder.encode(fn);
        System.out.println(data);
    }

}
