package global.smartup.node.controller;

import global.smartup.node.eth.EthUtil;
import global.smartup.node.util.MapBuilder;
import org.junit.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class TradeControllerTest {

    private static final String SutAddress = "0xf1899c6eb6940021c1ae4e9c3a8e29ee93704b03";
    private static final String MarketAddress = "0x8f27A8B0f1E28999e195C965ef9760C863c3Ea6D";
    private static final String MarketId = "g21kqui8a9s";

    @Test
    public void firstBuy() {
        String ctCount = "10";
        String gasLimit = "320000";
        String gasPrice = "50";
        String sellPrice = "12";
        String time = String.valueOf(System.currentTimeMillis());

        byte[] hash1 = EthUtil.soliditySha3(
            new Address(MarketAddress),
            new Uint256(Convert.toWei(new BigDecimal(ctCount), Convert.Unit.ETHER).toBigInteger()),
            new Address(OkHttpUtil.UserAddress),
            new Uint256(Convert.toWei(new BigDecimal(gasLimit).multiply(new BigDecimal(gasPrice)), Convert.Unit.GWEI).toBigInteger()),
            Hash.sha3(time.getBytes())
        );
        String sign = EthUtil.sign(OkHttpUtil.UserPrivateKey, Numeric.toHexString(hash1), false);

        byte[] hash2 = EthUtil.soliditySha3(
            new Uint256(Convert.toWei(new BigDecimal(ctCount), Convert.Unit.ETHER).toBigInteger()),
            new Uint256(Convert.toWei(new BigDecimal(sellPrice), Convert.Unit.ETHER).toBigInteger()),
            new Uint256(new BigInteger(time)),
            new Address(MarketAddress),
            new Address(SutAddress),
            new Address(OkHttpUtil.UserAddress)
        );
        String sellSign = EthUtil.sign(OkHttpUtil.UserPrivateKey, Numeric.toHexString(hash2), false);

        Map<String, String> param = new HashMap<>();
        param.put("ctCount", ctCount);
        param.put("gasLimit", gasLimit);
        param.put("gasPrice", gasPrice);
        param.put("marketId", MarketId);
        param.put("sign", sign);
        param.put("timestamp", time);
        param.put("sellPrice", sellPrice);
        param.put("sellSign", sellSign);

        OkHttpUtil.postWithLogin("/api/user/first/stage/buy", param);
    }

    @Test
    public void orderBook() {
        OkHttpUtil.post("/api/trade/order/book", MapBuilder.<String, String>create().put("marketId", "g21kqui8a9s").build());
    }

    @Test
    public void buy() {
        String price = "12";
        String volume = "10";
        String times = "1";
        String gasPrice = "50";
        Long timestamp = System.currentTimeMillis();
        String sellPrice = "14";

        String gasLimit = "300000";

        byte[] makeSignHash = EthUtil.soliditySha3(
            new Uint256(Convert.toWei(new BigDecimal(volume), Convert.Unit.ETHER).toBigInteger()),
            new Uint256(Convert.toWei(new BigDecimal(price), Convert.Unit.ETHER).toBigInteger()),
            new Uint256(timestamp),
            new Address(SutAddress),
            new Address(MarketAddress),
            new Address(OkHttpUtil.UserAddress)
        );
        String makeSign = EthUtil.sign(OkHttpUtil.UserPrivateKey, Numeric.toHexString(makeSignHash), false);

        BigInteger feeWei = Convert.toWei(new BigDecimal(gasLimit).multiply(new BigDecimal(gasPrice)), Convert.Unit.GWEI).toBigInteger();
        byte[] takeSignHash = EthUtil.soliditySha3(
            new Uint256(Convert.toWei(new BigDecimal(volume), Convert.Unit.ETHER).toBigInteger()),
            new Uint256(Convert.toWei(new BigDecimal(price), Convert.Unit.ETHER).toBigInteger()),
            new Uint256(timestamp),
            new Uint256(feeWei),
            new Address(SutAddress),
            new Address(MarketAddress),
            new Address(OkHttpUtil.UserAddress)
        );
        String takeSign = EthUtil.sign(OkHttpUtil.UserPrivateKey, Numeric.toHexString(takeSignHash), false);

        byte[] sellSignHash = EthUtil.soliditySha3(
            new Uint256(Convert.toWei(new BigDecimal(volume), Convert.Unit.ETHER).toBigInteger()),
            new Uint256(Convert.toWei(new BigDecimal(sellPrice), Convert.Unit.ETHER).toBigInteger()),
            new Uint256(timestamp),
            new Address(MarketAddress),
            new Address(SutAddress),
            new Address(OkHttpUtil.UserAddress)
        );
        String sellSign = EthUtil.sign(OkHttpUtil.UserPrivateKey, Numeric.toHexString(sellSignHash), false);

        Map<String, String> param = new HashMap<>();
        param.put("marketId", MarketId);
        param.put("type", "buy");
        param.put("price", price);
        param.put("volume", volume);
        param.put("times", times);
        param.put("timestamp", timestamp.toString());
        param.put("makeSign", makeSign);
        param.put("takeSign", takeSign);
        param.put("sellPrice", sellPrice);
        param.put("sellSign", sellSign);

        // OkHttpUtil.postWithLogin("/api/user/trade/add/buy/reckon", param);
        OkHttpUtil.postWithLogin("/api/user/trade/add/buy", param);
    }

    @Test
    public void cancel() {
        Map<String, String> param = new HashMap<>();
        param.put("tradeId", "gva5d34yi2o");
        OkHttpUtil.postWithLogin("/api/user/trade/cancel", param);
    }

    @Test
    public void updateSell() {
        List<Map<String, String>> newOrders = new ArrayList<>();
        newOrders.add(MapBuilder.<String, String>create()
            .put("price", "13")
            .put("volume", "10")
            .put("times", "1")
            .put("gasPrice", "50")
            .put("timestamp", String.valueOf(System.currentTimeMillis()))
            .build());

        for (Map<String, String> order : newOrders) {
            String price = order.get("price");
            String volume = order.get("volume");
            String times = order.get("price");
            String gasPrice = order.get("gasPrice");
            Long timestamp = Long.valueOf(order.get("timestamp"));

            String gasLimit = "300000";

            byte[] makeSignHash = EthUtil.soliditySha3(
                new Uint256(Convert.toWei(new BigDecimal(volume), Convert.Unit.ETHER).toBigInteger()),
                new Uint256(Convert.toWei(new BigDecimal(price), Convert.Unit.ETHER).toBigInteger()),
                new Uint256(timestamp),
                new Address(MarketAddress),
                new Address(SutAddress),
                new Address(OkHttpUtil.UserAddress)
            );
            String makeSign = EthUtil.sign(OkHttpUtil.UserPrivateKey, Numeric.toHexString(makeSignHash), false);
            order.put("makeSign", makeSign);

            BigInteger feeWei = Convert.toWei(new BigDecimal(gasLimit).multiply(new BigDecimal(gasPrice)), Convert.Unit.GWEI).toBigInteger();
            byte[] takeSignHash = EthUtil.soliditySha3(
                new Uint256(Convert.toWei(new BigDecimal(volume), Convert.Unit.ETHER).toBigInteger()),
                new Uint256(Convert.toWei(new BigDecimal(price), Convert.Unit.ETHER).toBigInteger()),
                new Uint256(timestamp),
                new Uint256(feeWei),
                new Address(MarketAddress),
                new Address(SutAddress),
                new Address(OkHttpUtil.UserAddress)
            );
            String takeSign = EthUtil.sign(OkHttpUtil.UserPrivateKey, Numeric.toHexString(takeSignHash), false);
            order.put("takeSign", takeSign);
        }

        Map<String, Object> param = new HashMap<>();
        param.put("marketId", MarketId);
        param.put("cancelOrderIds", Arrays.asList("gv9qmi9wav4"));
        param.put("lockOrderIds", new ArrayList<>());
        param.put("newOrders", newOrders);

        // OkHttpUtil.postJsonWithLogin("/api/user/trade/update/sell/reckon", param);
        OkHttpUtil.postJsonWithLogin("/api/user/trade/update/sell", param);
    }

}
