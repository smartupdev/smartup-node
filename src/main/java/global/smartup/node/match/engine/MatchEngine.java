package global.smartup.node.match.engine;

import global.smartup.node.match.bo.Order;
import global.smartup.node.match.common.Const;
import global.smartup.node.match.common.OrderType;
import global.smartup.node.match.service.OrderService;
import global.smartup.node.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public class MatchEngine {

    private static final Logger log = LoggerFactory.getLogger(MatchEngine.class);

    private OrderService orderService;

    private String marketId;

    private boolean isReady = false;

    private BigDecimal lastPrice = BigDecimal.ZERO;

    private BigDecimal currentPrice = BigDecimal.ZERO;

    private LinkedList<Map<String, Object>> latelyOrderChild;

    private OrderBook sellBook;

    private OrderBook buyBook;

    public MatchEngine(OrderService orderService,String marketId) {
        this.orderService = orderService;
        this.marketId = marketId;
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
            times = sellBook.takeCalculate(price, volume);
        } else {
            times = buyBook.takeCalculate(price, volume);
        }
        return times;
    }

    /**
     * 挂卖单
     * @param userId
     * @param price
     * @param volume
     * @param times
     * @return
     */
    public synchronized Map<String, Object> makeSellOrder(
        String userId, BigDecimal price, BigDecimal volume, Integer times, BigDecimal fee, String sign
    ) {
        Map<String, Object> ret = new HashMap<>();
        Integer t = buyBook.takeCalculate(price, volume);
        times = times == null ? 0 : times;
        if (t.compareTo(0) > times) {
            // 返回匹配次数
            ret.put("code", Const.FeeNotEnough);
            ret.put("obj", MapBuilder.create().put("times", times).build());
            return ret;
        }

        // save order
        String id = orderService.saveOrder(marketId, userId, OrderType.Sell.getValue(), price, volume, fee, sign);
        Order order = new Order();
        order.setOrderId(id)
            .setUserAddress(userId)
            .setEntrustPrice(price)
            .setEntrustVolume(volume)
            .setUnfilledVolume(volume)
            .setTimes(times)
            .setType(OrderType.Sell);

        // make order
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
     * @return
     */
    public synchronized Map<String, Object> makeBuyOrder(
        String userId, BigDecimal price, BigDecimal volume, Integer times, BigDecimal fee, String sign
    ) {
        Map<String, Object> ret = new HashMap<>();
        Integer t = sellBook.takeCalculate(price, volume);
        times = times == null ? 0 : times;
        if (t.compareTo(0) > times) {
            // 返回匹配次数
            ret.put("code", Const.FeeNotEnough);
            ret.put("obj", MapBuilder.create().put("times", times).build());
            return ret;
        }

        // save order
        String id = orderService.saveOrder(marketId, userId, OrderType.Buy.getValue(), price, volume, fee, sign);
        Order order = new Order();
        order.setOrderId(id)
            .setUserAddress(userId)
            .setEntrustPrice(price)
            .setEntrustVolume(volume)
            .setUnfilledVolume(volume)
            .setTimes(times)
            .setType(OrderType.Buy);

        // make order
        buyBook.makeOrder(order);

        // take order
        if (t.compareTo(0) > 0) {
            sellBook.take(order);
            buyBook.clearBucketOrder(order);
        }

        ret.put("code", Const.Success);
        ret.put("obj", MapBuilder.create().put("order", order).build());
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
     *   fee: xx
     *   sign: xx
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
        Integer times = 0;
        for (Map<String, Object> map : newOrders) {
            times += (Integer) map.get("times");
        }
        buyBook.takeCalculate(newOrders);
        Integer takeTimes = 0;
        for (Map<String, Object> map : newOrders) {
            takeTimes += (Integer) map.get("times");
        }
        if (times.compareTo(takeTimes) < 0) {
            ret.put("code", Const.FeeNotEnough);
            return ret;
        }

        // 修改移除订单部分
        sellBook.updateCancelLeft(cancelOrderIds);

        // 生成新订单
        // 匹配
        // 更新新订单
        List<Order> orders = new ArrayList<>();
        for (Map<String, Object> map : newOrders) {
            String id = orderService.saveOrder(marketId, userId,
                OrderType.Sell.getValue(),
                (BigDecimal) map.get("price"),
                (BigDecimal) map.get("volume"),
                (BigDecimal) map.get("fee"),
                map.get("sign").toString());

            Order o = new Order();
            o.setOrderId(id);
            o.setUserAddress(userId);
            o.setEntrustPrice((BigDecimal) map.get("price"));
            o.setEntrustVolume((BigDecimal) map.get("volume"));
            o.setUnfilledVolume((BigDecimal) map.get("volume"));
            o.setTimes((Integer) map.get("times"));
            o.setType(OrderType.Sell);
            orders.add(o);

            sellBook.makeOrder(o);

            if (o.getTimes().compareTo(0) > 0) {
                buyBook.take(o);
                sellBook.clearBucketOrder(o);
            }
        }

        ret.put("code", Const.Success);
        ret.put("orders", orders);
        return ret;
    }

    /**
     * 取消买单
     * @param userId
     * @param orderId
     * @return
     */
    public synchronized boolean cancelBuyOrder(String userId, String orderId) {
        return buyBook.cancelOrder(userId, orderId) != null;
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

    public OrderService getOrderService() {
        return orderService;
    }

    public String getMarketId() {
        return marketId;
    }
}
