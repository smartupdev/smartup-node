package global.smartup.node.eth;

import org.junit.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EthUtilTest {

    @Test
    public void soliditySha3() {
        Address address = new Address("0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E");
        Uint256 uint = new Uint256(BigInteger.valueOf(16));
        byte[] bytes = EthUtil.soliditySha3(address, uint);
        System.out.println(Numeric.toHexString(bytes));
    }

    @Test
    public void checkCrateMarketSign() {
        boolean ret = EthUtil.checkCrateMarketSign(
            "0xb44940be0eea81a3d0da22cc15208af4744bea8e",
            BigDecimal.valueOf(1),
            "1",
            "1",
            BigDecimal.valueOf(1),
            BigDecimal.valueOf(1),
            BigDecimal.valueOf(1),
            BigDecimal.valueOf(0.000000001),
            1L,
            "0xd1a832fc99401a7e668ec623f8bfe4d6ba549a8fd70373624fc542e5c7c9d40f779f2e4c712e0105918b62abbb54499d7ee1f224a77526fb176a03367d9ef08e1b");
        assert ret;
    }

    @Test
    public void checkBuyTakeSign() {
        boolean ret =EthUtil.checkBuyTakeSign(
            BigDecimal.valueOf(1),
            BigDecimal.valueOf(1),
            1L,
            BigDecimal.valueOf(1),
            "0xf1899c6eb6940021c1ae4e9c3a8e29ee93704b03",
            "0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E",
            "0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E",
            "0x2293b64c464cbc1903bc6420e1e82820dba88aaa49c78e4d2ce43e077b9635cd3730bd6255ddffd4c32e916037961e3fc468b91dd9df2c95ea1bdd97696d9b6c1c"
        );
        assert ret;
    }

    @Test
    public void recoverSignature() {
        String address = "0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E";

        String msg = "0xf2c09a4f6cc16286c57a61efe6a5a25bde63285d6b87580ddaffe5dc750cff05";
        String sign = "0x4b66c6d1e08ef075e27232ce39659a84b4ab71052f7287534a1883518a9b08fe6e82556b907faa54da354c6fd949cf0a3722575666c5da91c95682d8fd9386611c";

        // String msg = "123456";
        // String sign = "0xe9bdd98439373fac368c618e43d67591e6dcb9d6c925054bc36c7e923d25b8cd479af0bc59207d5645d347d14593ce06c6ed98c2082b4a1f363d1cd3a78dd6e71c";

        boolean ret = EthUtil.recoverSignature(address, msg, sign);
        assert ret;
    }

    @Test
    public void checkFistStageBuySign() {
        String userAddress = "0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E";
        String marketAddress = "0x8f27A8B0f1E28999e195C965ef9760C863c3Ea6D";
        BigDecimal ctAmount = BigDecimal.valueOf(10);
        BigDecimal fee = BigDecimal.valueOf(0.0096);
        String time = "123";
        String sign = "0x24cee67b74f6d05f5c7fa1da587b78827a36adaa0d6fac117c2a7bad3100c5310521390d65373058520b22a2a591cf00b21e9a5c518e484eaa44d5a87d7d64991c";
        boolean ret = EthUtil.checkFistStageBuySign(userAddress, marketAddress, ctAmount, fee, time, sign);
        assert ret;
    }

}
