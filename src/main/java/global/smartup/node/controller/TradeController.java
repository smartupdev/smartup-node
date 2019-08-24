package global.smartup.node.controller;

import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.EthUtil;
import global.smartup.node.match.service.MatchService;
import global.smartup.node.po.Market;
import global.smartup.node.po.Trade;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.TradeService;
import global.smartup.node.service.UserAccountService;
import global.smartup.node.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.web3j.utils.Convert;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Api(description = "交易")
@RestController
@RequestMapping("/api")
public class TradeController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private MatchService matchService;


    @ApiOperation(value = "交易详情", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：tradeId\n" +
                    "返回：obj = {\n" +
                    "　userAddress, marketId, type, state, entrustVolume, entrustPrice, filledVolume, avgPrice, fee, sign, createTime, updateTime \n" +
                    "　type(firstStageBuy/buy/sell), state(trading/cancel/cancelPart/done),\n" +
                    "　　childList = [\n" +
                    "　　　{\n" +
                    "　　　　childId, marketId, txHash, volume, price, createTime\n" +
                    "　　　　tx = {\n" +
                    "　　　　　txHash, stage, type, userAddress, detail, createTime, blockTime\n" +
                    "　　　　}\n" +
                    "　　　}\n" +
                    "　　]\n" +
                    "}")
    @RequestMapping("/trade/one")
    public Object tradeOne(HttpServletRequest request, String tradeId) {
        try {
            Trade trade = tradeService.queryById(tradeId);
            return Wrapper.success(trade);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户交易列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：types, states, pageNumb, pageSize\n" +
                    "返回：obj = {\n" +
                    "　list = [ 见/api/trade/one ]\n" +
                    "}")
    @RequestMapping("/user/trade/list")
    public Object userTradeList(HttpServletRequest request, String[] types, String[] states, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            types = types != null ? types : new String[0];
            states = states != null ? states : new String[0];
            Pagination page = tradeService.queryByUserTrade(userAddress, Arrays.asList(types), Arrays.asList(states), pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "第一阶段买入CT", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketAddress, ctCount, gasLimit, gasPrice, timestamp, sign\n" +
                        "返回：见/api/trade/one")
    @RequestMapping("/user/first/stage/buy")
    public Object firstStageBuy(HttpServletRequest request, String marketAddress, BigDecimal ctCount,
                                BigInteger gasLimit, BigInteger gasPrice, String timestamp, String sign) {
        try {
            String userAddress = getLoginUserAddress(request);

            // check market
            Market market = marketService.queryByAddress(marketAddress);
            if (market == null) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketNotExist));
            }

            // check gas price
            if (!Checker.isGasPriceRight(gasPrice)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.TransactionGasPriceError));
            }

            // check balance
            BigDecimal sut = BigDecimal.ZERO;
            BigDecimal gasFee = Convert.fromWei(new BigDecimal(gasPrice.multiply(gasLimit)), Convert.Unit.GWEI);
            Boolean hasEth = userAccountService.hasEnoughEth(userAddress, gasFee);
            Boolean hasSut = userAccountService.hasEnoughSut(userAddress, sut);
            if (hasEth == null || hasSut == null) {
                return Wrapper.alert(getLocaleMsg(LangHandle.NetWorkError));
            }
            if (!hasEth){
                return Wrapper.alert(getLocaleMsg(LangHandle.AccountEthNotEnough));
            }
            if (!hasSut){
                return Wrapper.alert(getLocaleMsg(LangHandle.AccountSutNotEnough));
            }

            Trade trade = tradeService.addFirstStageBuy(userAddress, market.getMarketId(), marketAddress, ctCount,
                market.getCtPrice(), gasLimit, gasPrice, timestamp, sign);
            if (trade == null) {
                return Wrapper.alert(getLocaleMsg(LangHandle.NetWorkError));
            }

            return Wrapper.success(trade);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "估算买入/卖出手续费", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketId, type(buy/sell), price, volume\n" +
                    "返回：{\n" +
                    "　code = EngineNotReady,\n" +
                    "　obj = {times, limit}\n" +
                    "}")
    @RequestMapping("/user/trade/test/match")
    public Object testMatch(HttpServletRequest request, String marketId, String type, BigDecimal price, BigDecimal volume) {
        try {
            Map<String, Object> map = matchService.queryMatchTime(marketId, type, price, volume);
            Wrapper wrapper = Wrapper.create(map);
            if (wrapper.wasSuccess()) {
                Map<String, Object> obj = (Map<String, Object>) wrapper.getObj();
                Integer times = (Integer) obj.get("times");
                obj.put("limit", EthUtil.getTradeGasLimit(times));
            }
            return Wrapper.create(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "买入/卖出", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketId, type(buy/sell), price, volume, times, gasPrice, sign\n" +
                    "返回：{\n" +
                    "　code = FeeNotEnough, SignError, MarketNotExist, TypeError, PriceCanNotLessZero, VolumeCanNotLessZero, GasPriceError\n" +
                    "　obj = {orderId, type, userAddress, entrustPrice, entrustVolume, unfilledVolume},\n" +
                    "}")
    @RequestMapping("/user/trade/add")
    public Object add(HttpServletRequest request, String marketId, String type, BigDecimal price, BigDecimal volume, Integer times, BigInteger gasPrice, String sign) {
        try {
            String userAddress = getLoginUserAddress(request);

            Market market = marketService.queryById(marketId);
            if (market == null) {
                return Wrapper.error(RespCode.Market.MarketNotExist);
            }

            if (!(PoConstant.Trade.Type.Buy.equals(type) || PoConstant.Trade.Type.Sell.equals(type))) {
                return Wrapper.error(RespCode.Trade.TypeError);
            }

            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                return Wrapper.error(RespCode.Trade.PriceCanNotLessZero);
            }

            if (volume == null || volume.compareTo(BigDecimal.ZERO) < 0) {
                return Wrapper.error(RespCode.Trade.VolumeCanNotLessZero);
            }

            times = times == null ? 0 : times;
            BigInteger limit = EthUtil.getTradeGasLimit(times);
            BigDecimal fee = new BigDecimal(limit.multiply(gasPrice));

            if (!Checker.isGasPriceRight(gasPrice)) {
                return Wrapper.error(RespCode.Trade.GasPriceError);
            }

            // TODO check sign

            Map<String, Object> map = new HashMap<>();
            if (PoConstant.Trade.Type.Buy.equals(type)) {
                map = matchService.addBuyOrder(marketId, userAddress, price, volume, times, fee, sign);
            } else if (PoConstant.Trade.Type.Sell.equals(type)) {
                map = matchService.addSellOrder(marketId, userAddress, price, volume, times, fee, sign);
            }

            return Wrapper.create(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "估算更新卖单手续费", httpMethod = "POST", response = Wrapper.class,
        notes = "参数：该接口参数为json { \n" +
            "　marketId, cancelOrderIds = [], lockOrderIds = [], \n" +
            "　newOrders = [ {mark, price, volume} ], \n" +
            "}\n" +
            "返回：{\n" +
            "　code = MarketNotExist, NewOrderNull, PriceCanNotLessZero, VolumeCanNotLessZero\n" +
            "　　EngineNotReady, OrderIdNull, OrderCanNotChange, NotYourOrder, VolumeNotMatch \n" +
            "　obj = [ {mark, times, limit} ]\n" +
            "}")
    @RequestMapping(value = "/user/trade/update/test/match", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updateTestMatch(HttpServletRequest request, @RequestBody UpdateOrder order) {
        try {
            String userAddress = getLoginUserAddress(request);

            Market market = marketService.queryById(order.getMarketId());
            if (market == null) {
                return Wrapper.error(RespCode.Market.MarketNotExist);
            }

            if (order.getCancelOrderIds().size() + order.getLockOrderIds().size() < 0) {
                return Wrapper.error(RespCode.Trade.UpdateSell.OrderIdNull);
            }

            if (order.getNewOrders().size() == 0) {
                return Wrapper.error(RespCode.Trade.NewOrderNull);
            }

            for (NewOrder newOrder : order.getNewOrders()) {
                if (newOrder.getPrice() == null || newOrder.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    return Wrapper.error(RespCode.Trade.PriceCanNotLessZero);
                }

                if (newOrder.getVolume() == null || newOrder.getVolume().compareTo(BigDecimal.ZERO) < 0) {
                    return Wrapper.error(RespCode.Trade.VolumeCanNotLessZero);
                }
            }

            List<Map<String, Object>> nos = new ArrayList<>();
            for (NewOrder newOrder : order.getNewOrders()) {
                nos.add(newOrder.toMap());
            }
            Map<String, Object> map = matchService.queryMatchTimeForUpdate(order.getMarketId(), userAddress,
                order.getCancelOrderIds(), order.getLockOrderIds(), nos);
            return Wrapper.create(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "更新卖单", httpMethod = "POST", response = Wrapper.class,
        notes = "参数：该接口参数为json { \n" +
            "　marketId, cancelOrderIds = [], lockOrderIds = [], \n" +
            "　newOrders = [ {price, volume, times, gasPrice, sign} ], \n" +
            "}\n" +
            "返回：{\n" +
            "　code = MarketNotExist, NewOrderNull, PriceCanNotLessZero, VolumeCanNotLessZero, SignError\n" +
            "　　EngineNotReady, OrderIdNull, OrderCanNotChange, NotYourOrder, VolumeNotMatch\n" +
            "　obj = [ {orderId, price, volume} ]\n" +
            "}")
    @RequestMapping("/user/trade/update")
    public Object tradeUpdate(HttpServletRequest request, @RequestBody UpdateOrder order) {
        try {
            String userAddress = getLoginUserAddress(request);

            Market market = marketService.queryById(order.getMarketId());
            if (market == null) {
                return Wrapper.error(RespCode.Market.MarketNotExist);
            }

            if (order.getCancelOrderIds().size() + order.getLockOrderIds().size() < 0) {
                return Wrapper.error(RespCode.Trade.UpdateSell.OrderIdNull);
            }

            if (order.getNewOrders().size() == 0) {
                return Wrapper.error(RespCode.Trade.NewOrderNull);
            }

            for (NewOrder newOrder : order.getNewOrders()) {
                if (newOrder.getPrice() == null || newOrder.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    return Wrapper.error(RespCode.Trade.PriceCanNotLessZero);
                }
                if (newOrder.getVolume() == null || newOrder.getVolume().compareTo(BigDecimal.ZERO) < 0) {
                    return Wrapper.error(RespCode.Trade.VolumeCanNotLessZero);
                }
                // fee
                if (newOrder.getTimes() == null) {
                    newOrder.setTimes(0);
                }
                if (!Checker.isGasPriceRight(newOrder.getGasPrice())) {
                    return Wrapper.error(RespCode.Trade.GasPriceError);
                }
                BigInteger gasLimit = EthUtil.getTradeGasLimit(newOrder.getTimes());
                BigDecimal fee = new BigDecimal(gasLimit.multiply(newOrder.getGasPrice()));
                newOrder.setFee(fee);
                // TODO check sign
            }

            List<Map<String, Object>> nos = new ArrayList<>();
            for (NewOrder newOrder : order.getNewOrders()) {
                nos.add(newOrder.toMap());
            }

            Map<String, Object> map = matchService.updateSellOrder(order.getMarketId(), userAddress, order.getCancelOrderIds(), order.getLockOrderIds(), nos);

            return Wrapper.create(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "取消买单", httpMethod = "POST", response = Wrapper.class,
        notes = "参数：marketId, tradeId\n" +
            "返回：{\n" +
            "　code = OrderAlreadyDone\n" +
            "}")
    @RequestMapping("/user/trade/cancel")
    public Object cancel(HttpServletRequest request, String marketId, String tradeId) {
        try {
            String userAddress = getLoginUserAddress(request);
            Map<String,Object> map =  matchService.cancelBuyOrder(marketId, userAddress, tradeId);
            return Wrapper.create(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "OrderBook [cached]", httpMethod = "POST", response = Wrapper.class,
        notes = "参数：marketId, topOfBook, topOfOrder\n" +
            "返回：obj = {\n" +
            "　currentPrice,lastPrice,\n" +
            "　orders = [ {time, price, volume} ]\n" +
            "　sellBook, buyBook = [ {price, volume} ]\n" +
            "}")
    @RequestMapping("/trade/order/book")
    public Object orderBook(HttpServletRequest request, String marketId, Integer topOfBook, Integer topOfOrder) {
        try {
            topOfBook = topOfBook == null ? 10 : topOfBook;
            topOfOrder = topOfOrder == null ? 20 : topOfOrder;
            Map<String, Object> map = matchService.queryOrderBook(marketId, topOfBook, topOfOrder);
            return Wrapper.create(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    // @ApiOperation(value = "市场的交易列表", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：TODO \n" +
    //                     "返回：")
    // @RequestMapping("/trade/market/list")
    // public Object tradeList(HttpServletRequest request) {
    //     try {
    //
    //         return Wrapper.success();
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }

    static class UpdateOrder {

        String marketId;
        List<String> cancelOrderIds;
        List<String> lockOrderIds;
        List<NewOrder> newOrders;

        public String getMarketId() {
            return marketId;
        }

        public UpdateOrder setMarketId(String marketId) {
            this.marketId = marketId;
            return this;
        }

        public List<String> getCancelOrderIds() {
            return cancelOrderIds;
        }

        public UpdateOrder setCancelOrderIds(List<String> cancelOrderIds) {
            this.cancelOrderIds = cancelOrderIds;
            return this;
        }

        public List<String> getLockOrderIds() {
            return lockOrderIds;
        }

        public UpdateOrder setLockOrderIds(List<String> lockOrderIds) {
            this.lockOrderIds = lockOrderIds;
            return this;
        }

        public List<NewOrder> getNewOrders() {
            return newOrders;
        }

        public UpdateOrder setNewOrders(List<NewOrder> newOrders) {
            this.newOrders = newOrders;
            return this;
        }

    }

    static class NewOrder {

        String mark;
        BigDecimal price;
        BigDecimal volume;
        Integer times;
        BigDecimal fee;
        BigInteger gasPrice;
        String sign;


        public String getMark() {
            return mark;
        }

        public NewOrder setMark(String mark) {
            this.mark = mark;
            return this;
        }

        public BigInteger getGasPrice() {
            return gasPrice;
        }

        public NewOrder setGasPrice(BigInteger gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public Integer getTimes() {
            return times;
        }

        public NewOrder setTimes(Integer times) {
            this.times = times;
            return this;
        }

        public BigDecimal getFee() {
            return fee;
        }

        public NewOrder setFee(BigDecimal fee) {
            this.fee = fee;
            return this;
        }

        public String getSign() {
            return sign;
        }

        public NewOrder setSign(String sign) {
            this.sign = sign;
            return this;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public NewOrder setPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public BigDecimal getVolume() {
            return volume;
        }

        public NewOrder setVolume(BigDecimal volume) {
            this.volume = volume;
            return this;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("price", price);
            map.put("volume", volume);
            return map;
        }
    }

}
