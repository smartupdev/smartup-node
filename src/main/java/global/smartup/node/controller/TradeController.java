package global.smartup.node.controller;

import global.smartup.node.Config;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.EthUtil;
import global.smartup.node.match.service.MatchService;
import global.smartup.node.po.Market;
import global.smartup.node.po.Trade;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.TradeService;
import global.smartup.node.service.UserAccountService;
import global.smartup.node.util.Checker;
import global.smartup.node.util.Pagination;
import global.smartup.node.util.RespCode;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @Autowired
    private Config config;

    @ApiOperation(value = "交易详情", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：tradeId\n" +
                    "返回：obj = {\n" +
                    "　userAddress, marketId, type, state, entrustVolume, entrustPrice, filledVolume, avgPrice, fee, sign, createTime, updateTime \n" +
                    "　type(firstStageBuy/plan/buy/sell), state(trading/cancel/cancelPart/done),\n" +
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
        notes = "参数：marketId, ctCount, gasLimit, gasPrice, timestamp, sign, sellPrice, sellSign\n" +
            "返回：{\n" +
            "　code = MarketNotExist, MarketCanNotTrade, GasPriceError, NetWorkError, SutNotEnough, EthNotEnough, SignError, SellSignError\n" +
            "　obj = {见/api/trade/one}\n" +
            "}")
    @RequestMapping("/user/first/stage/buy")
    public Object firstStageBuy(
        HttpServletRequest request, String marketId, BigDecimal ctCount, BigInteger gasLimit, BigInteger gasPrice,
        Long timestamp, String sign, BigDecimal sellPrice, String sellSign
    ) {
        try {
            String userAddress = getLoginUserAddress(request);

            // check market
            Market market = marketService.queryById(marketId);
            if (market == null) {
                return Wrapper.error(RespCode.Market.MarketNotExist);
            }
            if (!PoConstant.Market.Status.Open.equals(market.getStatus()) || !PoConstant.Market.Stage.First.equals(market.getStage())) {
                return Wrapper.error(RespCode.Market.MarketCanNotTrade);
            }
            // TODO check market expire

            // check gas price
            if (!Checker.isGasPriceRight(gasPrice)) {
                return Wrapper.error(RespCode.Trade.GasPriceError);
            }

            // check balance
            BigDecimal sut = ctCount.multiply(market.getCtPrice());
            BigDecimal fee = Convert.fromWei(new BigDecimal(gasPrice.multiply(gasLimit)), Convert.Unit.GWEI);
            Boolean hasEth = userAccountService.hasEnoughEth(userAddress, fee);
            Boolean hasSut = userAccountService.hasEnoughSut(userAddress, sut);
            if (!hasEth) {
                return Wrapper.error(RespCode.Trade.EthNotEnough);
            }
            if (!hasSut) {
                return Wrapper.error(RespCode.Trade.SutNotEnough);
            }

            // check sign
            boolean signRight = EthUtil.checkFistStageBuySign(userAddress, market.getMarketAddress(), ctCount, fee, String.valueOf(timestamp), sign);
            if (!signRight) {
                return Wrapper.error(RespCode.Trade.SignError);
            }
            boolean sellSignRight = EthUtil.checkSellMakeSign(userAddress, market.getMarketAddress(), config.ethSutContract,
                ctCount, sellPrice, timestamp, sellSign);
            if (!sellSignRight) {
                return Wrapper.error(RespCode.Trade.SellSignError);
            }

            // save buy order
            Trade trade = tradeService.addFirstStageBuy(userAddress, market.getMarketId(), market.getMarketAddress(), ctCount,
                market.getCtPrice(), gasLimit, gasPrice, timestamp, sign, sellPrice, sellSign);
            if (trade == null) {
                return Wrapper.error(RespCode.Trade.NetWorkError);
            }

            return Wrapper.success(trade);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "估算买入手续费", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketId, type(buy), price, volume\n" +
                    "返回：{\n" +
                    "　code = EngineNotReady\n" +
                    "　obj = {times, limit}\n" +
                    "}")
    @RequestMapping("/user/trade/add/buy/reckon")
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

    @ApiOperation(value = "添加买单", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketId, price, volume, times, timestamp, makeSign, takeSign, sellPrice, sellSign\n" +
                    "返回：{\n" +
                    "　code = FeeNotEnough, MakeSignError, TakeSignError, SellSignError, MarketNotExist, TypeError, PriceCanNotLessZero, \n" +
                    "　　VolumeCanNotLessZero, GasPriceError\n" +
                    "　obj = {见/api/trade/one}\n" +
                    "}")
    @RequestMapping("/user/trade/add/buy")
    public Object add(HttpServletRequest request, String marketId,BigDecimal price, BigDecimal volume,
                      Integer times, Long timestamp, String makeSign, String takeSign,
                      BigDecimal sellPrice, String sellSign) {
        try {
            String userAddress = getLoginUserAddress(request);
            Market market = marketService.queryById(marketId);
            if (market == null) {
                return Wrapper.error(RespCode.Market.MarketNotExist);
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                return Wrapper.error(RespCode.Trade.PriceCanNotLessZero);
            }
            if (volume == null || volume.compareTo(BigDecimal.ZERO) < 0) {
                return Wrapper.error(RespCode.Trade.VolumeCanNotLessZero);
            }

            // TODO check sell price

            times = times == null ? 0 : times;
            BigInteger gasLimit = EthUtil.getTradeGasLimit(times);
            BigInteger gasPrice = BigInteger.valueOf(config.ethGasPrice);
            BigDecimal fee = Convert.fromWei(new BigDecimal(gasLimit.multiply(gasPrice)), Convert.Unit.GWEI);

            // check sign
            boolean makeSignOk = EthUtil.checkBuyMakeSign(userAddress, market.getMarketAddress(), config.ethSutContract, volume, price, timestamp, makeSign);
            if (!makeSignOk) {
                return Wrapper.error(RespCode.Trade.MakeSignError);
            }
            if (times.compareTo(0) > 0) {
                 boolean takeSignOk = EthUtil.checkBuyTakeSign(price, volume, timestamp, fee, market.getMarketAddress(), config.ethSutContract, userAddress, takeSign);
                if (!takeSignOk) {
                    return Wrapper.error(RespCode.Trade.TakeSignError);
                }
            }
            boolean sellSignOk = EthUtil.checkSellMakeSign(userAddress, market.getMarketAddress(), config.ethSutContract, volume, sellPrice, timestamp, sellSign);
            if (!sellSignOk) {
                return Wrapper.error(RespCode.Trade.SellSignError);
            }

            // check balance
            Boolean hasEth = userAccountService.hasEnoughEth(userAddress, fee);
            if (hasEth == null) {
                return Wrapper.alert(getLocaleMsg(LangHandle.NetWorkError));
            }
            if (!hasEth){
                return Wrapper.alert(getLocaleMsg(LangHandle.AccountEthNotEnough));
            }
            BigDecimal sut = price.multiply(volume);
            Boolean hasSut = userAccountService.hasEnoughSut(userAddress, sut);
            if (!hasSut){
                return Wrapper.alert(getLocaleMsg(LangHandle.AccountSutNotEnough));
            }

            Map<String, Object> map = matchService.addBuyOrder(marketId, userAddress, price, volume, times,
                gasPrice.longValue(), gasLimit.longValue(), timestamp, makeSign, takeSign);

            // trade plan
            if (RespCode.Success.equals(map.get("code"))) {
                Trade trade = (Trade) map.get("obj");
                tradeService.addMakePlan(trade.getTradeId(), sellPrice, timestamp, sellSign);
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
    @RequestMapping(value = "/user/trade/update/sell/reckon", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
            "　newOrders = [ {price, volume, times, timestamp, makeSign, takeSign} ], \n" +
            "}\n" +
            "返回：{\n" +
            "　code = MarketNotExist, NewOrderNull, PriceCanNotLessZero, VolumeCanNotLessZero, MakeSignError, TakeSignError\n" +
            "　　EngineNotReady, OrderIdNull, OrderCanNotChange, NotYourOrder, VolumeNotMatch\n" +
            "　obj = [ {orderId, price, volume} ]\n" +
            "}")
    @RequestMapping("/user/trade/update/sell")
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

            // TODO check order balance

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
                BigInteger gasPrice = BigInteger.valueOf(config.ethGasPrice);
                BigInteger gasLimit = EthUtil.getTradeGasLimit(newOrder.getTimes());
                BigDecimal fee = Convert.fromWei(new BigDecimal(gasLimit.multiply(gasPrice)), Convert.Unit.GWEI);
                newOrder.setGasPrice(gasPrice.longValue());
                newOrder.setGasLimit(gasLimit.longValue());

                // TODO check timestamp

                // check sign
                boolean makeSignOk = EthUtil.checkSellMakeSign(userAddress, market.getMarketAddress(), config.ethSutContract,
                    newOrder.getVolume(), newOrder.getPrice(), newOrder.getTimestamp(), newOrder.getMakeSign());
                if (!makeSignOk) {
                    return Wrapper.error(RespCode.Trade.MakeSignError);
                }
                boolean takeSignOk = EthUtil.checkSellTakeSign(newOrder.getPrice(), newOrder.getVolume(), newOrder.getTimestamp(), fee,
                    market.getMarketAddress(), config.ethSutContract, userAddress, newOrder.getTakeSign());
                if (!takeSignOk) {
                    return Wrapper.error(RespCode.Trade.TakeSignError);
                }
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
        notes = "参数：tradeId\n" +
            "返回：{\n" +
            "　code = OrderNotExist, OrderCanNotCancel, OrderAlreadyDone\n" +
            "}")
    @RequestMapping("/user/trade/cancel")
    public Object cancel(HttpServletRequest request, String tradeId) {
        try {
            String userAddress = getLoginUserAddress(request);
            Trade trade = tradeService.queryById(tradeId);
            if (trade == null) {
                return Wrapper.error(RespCode.Trade.OrderNotExist);
            }
            if (!userAddress.equals(trade.getUserAddress())) {
                return Wrapper.error(RespCode.Trade.OrderNotExist);
            }
            if (!PoConstant.Trade.State.Trading.equals(trade.getState())) {
                return Wrapper.error(RespCode.Trade.OrderCanNotCancel);
            }
            Map<String,Object> map =  matchService.cancelBuyOrder(trade.getMarketId(), tradeId);
            return Wrapper.create(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "OrderBook [cached]", httpMethod = "POST", response = Wrapper.class,
        notes = "参数：marketId, topOfBook, topOfOrder\n" +
            "返回：obj = {\n" +
            "　currentPrice, lastPrice,\n" +
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
    //             notes = "参数： \n" +
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
        Long gasPrice;
        Long gasLimit;
        Long timestamp;
        String makeSign;
        String takeSign;

        public Long getGasPrice() {
            return gasPrice;
        }

        public NewOrder setGasPrice(Long gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public Long getGasLimit() {
            return gasLimit;
        }

        public NewOrder setGasLimit(Long gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public NewOrder setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public String getTakeSign() {
            return takeSign;
        }

        public NewOrder setTakeSign(String takeSign) {
            this.takeSign = takeSign;
            return this;
        }

        public String getMark() {
            return mark;
        }

        public NewOrder setMark(String mark) {
            this.mark = mark;
            return this;
        }

        public Integer getTimes() {
            return times;
        }

        public NewOrder setTimes(Integer times) {
            this.times = times;
            return this;
        }

        public String getMakeSign() {
            return makeSign;
        }

        public NewOrder setMakeSign(String makeSign) {
            this.makeSign = makeSign;
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
            map.put("mark", mark);
            map.put("price", price);
            map.put("volume", volume);
            map.put("times", times);
            map.put("gasPrice", gasPrice);
            map.put("gasLimit", gasLimit);
            map.put("timestamp", timestamp);
            map.put("makeSign", makeSign);
            map.put("takeSign", takeSign);
            return map;
        }
    }

}
