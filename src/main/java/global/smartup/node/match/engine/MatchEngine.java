package global.smartup.node.match.engine;

import global.smartup.node.match.bo.Order;
import global.smartup.node.match.bo.OrderChild;
import global.smartup.node.match.common.Const;
import global.smartup.node.match.common.OrderType;
import global.smartup.node.match.service.MOrderService;
import global.smartup.node.po.Trade;
import global.smartup.node.po.TradeChild;
import global.smartup.node.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class MatchEngine {

    private static final Logger log = LoggerFactory.getLogger(MatchEngine.class);

    private MOrderService orderService;

    private String marketId;

    private String marketAddress;

    private boolean isReady = false;

    private BigDecimal lastPrice = BigDecimal.ZERO;

    private BigDecimal currentPrice = BigDecimal.ZERO;

    private LinkedList<Map<String, Object>> latelyOrderChild;

    private OrderBook sellBook;

    private OrderBook buyBook;

    public MatchEngine(MOrderService orderService, String marketId, String marketAddress) {
        this.orderService = orderService;
        this.marketId = marketId;
        this.marketAddress = marketAddress;
        this.latelyOrderChild = new LinkedList<>();
        this.sellBook = new OrderBook(this, OrderType.Sell, null);
        this.buyBook = new OrderBook(this, OrderType.Buy, null);
    }

    public void loadOrder(Order order) {
        if (OrderType.Buy.equals(order.getType())) {
            buyBook.loadOrder(order);
        } else {
            sellBook.loadOrder(order);
        }
    }

    public void loadLatelyOrder(Date time, BigDecimal price, BigDecimal volume) {
        if (latelyOrderChild.size() > 99) {
            latelyOrderChild.removeLast();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("time", time);
        map.put("price", price);
        map.put("volume", volume);
        latelyOrderChild.addFirst(map);
    }

    public void ready() {
        this.isReady = true;
    }

    /**
     * 查询匹配次数
     * @param type
     * @param price
     * @param volume
     * @return
     */
    public synchronized Integer queryMatchTimes(String type, BigDecimal price, BigDecimal volume) {
        Integer times;
        if (OrderType.Sell.getValue().equals(type)) {
            times = buyBook.takeCalculate(price, volume);
        } else {
            times = sellBook.takeCalculate(price, volume);
        }
        return times;
    }

    /**
     * 第一阶段买入后，挂卖单
     * @param userId
     * @param price
     * @param volume
     * @return
     */
    public synchronized Map<String, Object> makeSellOrder(
        String userId, BigDecimal price, BigDecimal volume, Long timestamp, String makeSign
    ) {
        Map<String, Object> ret = new HashMap<>();
        Integer t = buyBook.takeCalculate(price, volume);

        // save trade
        Trade trade = orderService.saveOrder(marketId, marketAddress, userId, OrderType.Sell.getValue(), price, volume, BigDecimal.ZERO, timestamp, makeSign);

        // make order
        Order order = new Order();
        order.setOrderId(trade.getTradeId())
            .setUserAddress(userId)
            .setEntrustPrice(price)
            .setEntrustVolume(volume)
            .setUnfilledVolume(volume)
            .setTimes(0)
            .setType(OrderType.Sell);
        sellBook.makeOrder(order);

        // take order
        if (t.compareTo(0) > 0) {
            buyBook.take(order);
            sellBook.clearBucketOrder(order);
        }

        ret.put("code", Const.Success);
        ret.put("obj", MapBuilder.create().put("order", order).build());
        return ret;
    }

    /**
     * 挂买单
     * @param userId
     * @param price
     * @param volume
     * @param times
     * @param gasPrice
     * @param gasLimit
     * @param timestamp
     * @param makeSign
     * @param takeSign
     * @return
     */
    public synchronized Map<String, Object> makeBuyOrder(
        String userId, BigDecimal price, BigDecimal volume, Integer times, Long gasPrice, Long gasLimit, Long timestamp,
        String makeSign, String takeSign
    ) {
        Map<String, Object> ret = new HashMap<>();
        Integer t = sellBook.takeCalculate(price, volume);
        times = times == null ? 0 : times;
        if (t.compareTo(times) > 0) {
            // 返回匹配次数
            ret.put("code", Const.FeeNotEnough);
            ret.put("obj", MapBuilder.create().put("times", times).build());
            return ret;
        }

        // save order
        BigDecimal fee = Convert.fromWei(new BigDecimal(BigInteger.valueOf(gasPrice).multiply(BigInteger.valueOf(gasLimit))), Convert.Unit.GWEI);
        Trade trade = orderService.saveOrder(marketId, marketAddress, userId, OrderType.Buy.getValue(), price, volume, fee, timestamp, makeSign);
        Order order = new Order();
        order.setOrderId(trade.getTradeId())
            .setUserAddress(userId)
            .setEntrustPrice(price)
            .setEntrustVolume(volume)
            .setUnfilledVolume(volume)
            .setTimes(times)
            .setType(OrderType.Buy);

        // add take plan
        String takePlanId = orderService.addTakePlan(trade.getTradeId(), t, gasPrice, gasLimit, timestamp, takeSign);

        // make order
        buyBook.makeOrder(order);

        // take order
        if (t.compareTo(0) > 0) {
            List<OrderChild> orderChild = sellBook.take(order);
            buyBook.clearBucketOrder(order);

            // save child
            List<TradeChild> tradeChild = orderService.saveChildUpdateTrade(marketId, takePlanId, orderChild);
            trade.setChildList(tradeChild);
        }

        ret.put("code", Const.Success);
        ret.put("obj", trade);
        return ret;
    }

    /**
     * 查询更新卖单价格匹配次数
     * @param userId
     * @param cancelOrderIds
     * @param lockOrderIds
     * @param newOrders
     *   price: xx
     *   volume: xx
     * @return
     */
    public synchronized Map<String, Object> queryMatchTimesForUpdate(
        String userId,
        List<String> cancelOrderIds,
        List<String> lockOrderIds,
        List<Map<String, Object>> newOrders
    ) {
        Map<String, Object> ret = new HashMap<>();

        // 检查userId，修改的量
        BigDecimal oldVol = BigDecimal.ZERO;
        List<Order> oldOrders = new ArrayList<>();
        for (String id : cancelOrderIds) {
            Order o = sellBook.getOrder(id);
            if (o == null || o.getUnfilledVolume().compareTo(BigDecimal.ZERO) <= 0) {
                ret.put("code", Const.OrderCanNotChange);
                return ret;
            }
            oldVol = oldVol.add(o.getUnfilledVolume());
            oldOrders.add(o);
            if (!o.getUserAddress().equals(userId)) {
                ret.put("code", Const.NotYourOrder);
                return ret;
            }
        }
        BigDecimal newVol = BigDecimal.ZERO;
        for (Map<String, Object> map : newOrders) {
            BigDecimal price = (BigDecimal) map.get("volume");
            newVol = newVol.add(price);
        }
        if (oldVol.compareTo(newVol) != 0) {
            ret.put("code", Const.VolumeNotMatch);
            return ret;
        }

        // 检查匹配次数
        buyBook.takeCalculate(newOrders);

        ret.put("code", Const.Success);
        ret.put("obj", newOrders);
        return ret;
    }

    /**
     * 更新卖单价格
     * 可能会匹配到订单
     * @param userId
     * @param cancelOrderIds
     * @param lockOrderIds
     * @param newOrders
     *   price: xx
     *   volume: xx
     *   times: xx
     *   gasPrice: xx
     *   gasLimit: xx
     *   timestamp: xx
     *   makeSign: xx
     *   takeSign: xx
     * @return
     */
    public synchronized Map<String, Object> updateSellOrder(
        String userId,
        List<String> cancelOrderIds,
        List<String> lockOrderIds,
        List<Map<String, Object>> newOrders
    ) {
        Map<String, Object> ret = new HashMap<>();

        // 检查userId，修改的量
        BigDecimal oldVol = BigDecimal.ZERO;
        List<Order> oldOrders = new ArrayList<>();
        for (String id : cancelOrderIds) {
            Order o = sellBook.getOrder(id);
            if (o == null || o.getUnfilledVolume().compareTo(BigDecimal.ZERO) <= 0) {
                ret.put("code", Const.OrderCanNotChange);
                return ret;
            }
            oldVol = oldVol.add(o.getUnfilledVolume());
            oldOrders.add(o);
            if (!o.getUserAddress().equals(userId)) {
                ret.put("code", Const.NotYourOrder);
                return ret;
            }
        }
        BigDecimal newVol = BigDecimal.ZERO;
        for (Map<String, Object> map : newOrders) {
            BigDecimal price = (BigDecimal) map.get("volume");
            newVol = newVol.add(price);
        }
        if (oldVol.compareTo(newVol) != 0) {
            ret.put("code", Const.VolumeNotMatch);
            return ret;
        }

        // 检查匹配次数
        Integer timesCount = 0;
        for (Map<String, Object> map : newOrders) {
            timesCount += (Integer) map.get("times");
        }
        buyBook.takeCalculate(newOrders);
        Integer takeTimes = 0;
        for (Map<String, Object> map : newOrders) {
            takeTimes += (Integer) map.get("times");
        }
        if (timesCount.compareTo(takeTimes) < 0) {
            ret.put("code", Const.FeeNotEnough);
            return ret;
        }

        // 修改移除订单部分
        sellBook.updateCancelLeft(cancelOrderIds);

        List<Trade> tradeList = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        for (Map<String, Object> map : newOrders) {
            BigDecimal price = (BigDecimal) map.get("price");
            BigDecimal volume = (BigDecimal) map.get("volume");
            Integer times = (Integer) map.get("times");
            Long gasPrice = (Long) map.get("gasPrice");
            Long gasLimit = (Long) map.get("gasLimit");
            Long timestamp = (Long) map.get("timestamp");
            String makeSign = (String) map.get("makeSign");
            String takeSign = (String) map.get("takeSign");

            // save trade
            BigDecimal fee = Convert.fromWei(new BigDecimal(BigInteger.valueOf(gasPrice).multiply(BigInteger.valueOf(gasLimit))), Convert.Unit.GWEI);
            Trade trade = orderService.saveOrder(marketId, marketAddress, userId, OrderType.Sell.getValue(), price, volume, fee, timestamp, makeSign);
            tradeList.add(trade);

            // save take plan
            String takePlanId = orderService.addTakePlan(trade.getTradeId(), times, gasPrice, gasLimit, timestamp, takeSign);

            // make order
            Order o = new Order();
            o.setOrderId(trade.getTradeId());
            o.setUserAddress(userId);
            o.setEntrustPrice(price);
            o.setEntrustVolume(volume);
            o.setUnfilledVolume(volume);
            o.setTimes(times);
            o.setType(OrderType.Sell);
            orders.add(o);
            sellBook.makeOrder(o);

            // take
            if (o.getTimes().compareTo(0) > 0) {
                List<OrderChild> orderChild = buyBook.take(o);
                sellBook.clearBucketOrder(o);

                // save child
                List<TradeChild> tradeChild = orderService.saveChildUpdateTrade(marketId, takePlanId, orderChild);
                trade.setChildList(tradeChild);
            }
        }

        ret.put("code", Const.Success);
        ret.put("obj", tradeList);
        return ret;
    }

    /**
     * 取消买单
     * @param orderId
     * @return
     */
    public synchronized boolean cancelBuyOrder(String orderId) {
        return buyBook.cancelOrder(orderId) != null;
    }

    /**
     * TODO 锁定订单
     * @param orderId
     * @param volume
     * @return
     */
    public synchronized Object lock(String orderId, BigDecimal volume) {

        return null;
    }

    /**
     * 查询 order book
     * @param topOfBook
     * @param topOfOrder
     * @return
     */
    public Map<String, Object> queryOrderBook(Integer topOfBook, Integer topOfOrder) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("code", Const.Success);
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("lastPrice", lastPrice);
        obj.put("currentPrice", currentPrice);
        obj.put("orders", queryLatelyOrder(topOfOrder));
        obj.put("sellBook", sellBook.queryOrderBook(topOfBook));
        obj.put("buyBook", buyBook.queryOrderBook(topOfBook));
        ret.put("obj", obj);
        return ret;
    }

    private List<Map<String, Object>> queryLatelyOrder(Integer top) {
        if (top.compareTo(latelyOrderChild.size()) > 0) {
            top = latelyOrderChild.size();
        }
        return latelyOrderChild.subList(0, top);
    }

    public void updateCurrentPrice(BigDecimal price) {
        this.lastPrice = this.currentPrice;
        this.currentPrice = price;
    }

    public MatchEngine setIsReady(boolean ready) {
        isReady = ready;
        return this;
    }

    public boolean isReady() {
        return isReady;
    }

    public MOrderService getOrderService() {
        return orderService;
    }

    public String getMarketId() {
        return marketId;
    }
}
