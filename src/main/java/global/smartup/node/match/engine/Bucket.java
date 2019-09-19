package global.smartup.node.match.engine;

import global.smartup.node.match.bo.Order;
import global.smartup.node.match.bo.OrderChild;
import global.smartup.node.match.common.OrderType;
import global.smartup.node.match.service.MOrderService;
import global.smartup.node.po.TradeChild;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Bucket {

    private static final Logger log = LoggerFactory.getLogger(Bucket.class);

    private MatchEngine engine;

    private MOrderService orderService;

    private String marketId;

    private OrderBook book;

    private BigDecimal price;

    private BigDecimal volume;

    private LinkedList<Order> orders;

    // 缓存批量订单估算时，缓存扣除的量，等待估算完成后，再恢复
    // orderId -> volume
    private Map<String, BigDecimal> volumeCache;


    public Bucket(MatchEngine engine, OrderBook book, BigDecimal price) {
        this.engine = engine;
        this.orderService = engine.getOrderService();
        this.marketId = engine.getMarketId();
        this.book = book;
        this.price = price;
        this.volume = BigDecimal.ZERO;
        this.orders = new LinkedList<>();
        this.volumeCache = new HashMap<>();
    }

    public void addOrder(Order order) {
        assert order != null;
        assert order.getEntrustVolume() != null;
        orders.addLast(order);
        volume = volume.add(order.getEntrustVolume());
    }

    /**
     * 匹配估算
     * @param times 已匹配的次数
     * @param volume 剩余需要匹配的量
     * @return 匹配的次数, 剩余需要匹配的量
     */
    public Pair<Integer, BigDecimal> takeCalculate(Integer times, BigDecimal volume) {
        BigDecimal b = volume;
        Integer t = times;
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            if (b.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            t += 1;
            Order o = iterator.next();
            if (o.getUnfilledVolume().compareTo(b) >= 0) {
                b = BigDecimal.ZERO;
                break;
            } else {
                b = b.subtract(o.getUnfilledVolume());
            }
        }
        return Pair.of(t, b);
    }

    /**
     * 匹配估算，缓存数据
     * @param times 已匹配的次数
     * @param volume 剩余需要匹配的量
     * @return 匹配的次数, 剩余需要匹配的量
     */
    public Pair<Integer, BigDecimal> takeCalculateWithCache(Integer times, BigDecimal volume) {
        BigDecimal vol = volume;
        Integer t = times;
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            if (vol.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            Order o = iterator.next();
            if (o.getUnfilledVolume().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            t += 1;
            if (o.getUnfilledVolume().compareTo(vol) >= 0) {
                cache(o.getOrderId(), vol);
                vol = BigDecimal.ZERO;
                break;
            } else {
                vol = vol.subtract(o.getUnfilledVolume());
                cache(o.getOrderId(), o.getUnfilledVolume());
            }
        }
        return Pair.of(t, vol);
    }

    /**
     * 恢复缓存
     */
    public void clearCache() {
        if (volumeCache.size() <= 0) {
            return;
        }
        volumeCache.forEach((id, vol) -> {
            Order o = getOrder(id);
            o.setUnfilledVolume(o.getUnfilledVolume().add(vol));
        });
        volumeCache.clear();
    }

    public List<OrderChild> take(Order take) {
        Iterator<Order> iterator = orders.iterator();
        List<OrderChild> children = new ArrayList<>();
        while (iterator.hasNext()) {
            if (take.getUnfilledVolume().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (take.getTimes().compareTo(0) <= 0) {
                break;
            }
            Order make = iterator.next();
            BigDecimal tradeVol;
            if (make.getUnfilledVolume().compareTo(take.getUnfilledVolume()) > 0) {
                tradeVol = take.getUnfilledVolume();
            } else {
                tradeVol = make.getUnfilledVolume();
            }

            // 更新 make
            make.setUnfilledVolume(make.getUnfilledVolume().subtract(tradeVol));
            if (make.getUnfilledVolume().compareTo(BigDecimal.ZERO) <= 0) {
                iterator.remove();
                book.removeOrderMap(make.getOrderId());
            }
            updateVolume();

            // 更新 take
            take.setTimes(take.getTimes() - 1);
            take.setUnfilledVolume(take.getUnfilledVolume().subtract(tradeVol));

            // 更新 bucket
            this.volume = this.volume.subtract(tradeVol);

            // 添加最新成交订单
            engine.loadLatelyOrder(new Date(), price, tradeVol);

            // 更新主订单，添加子订单
            String buyId, sellId;
            if (OrderType.Buy.equals(take.getType())) {
                buyId = take.getOrderId();
                sellId = make.getOrderId();
            } else {
                buyId = make.getOrderId();
                sellId = take.getOrderId();
            }
            OrderChild child = new OrderChild();
            child.setBuyOrderId(buyId).setSellOrderId(sellId).setPrice(price).setVolume(tradeVol);
            children.add(child);
        }
        return children;
    }

    public Order removeOrder(String orderId) {
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order o = iterator.next();
            if (o.getOrderId().equals(orderId)) {
                iterator.remove();
                return o;
            }
        }
        return null;
    }

    public void updateVolume() {
        BigDecimal vol = BigDecimal.ZERO;
        for (Order o : orders) {
            vol = vol.add(o.getUnfilledVolume());
        }
        this.volume = vol;
    }

    public List<BigDecimal> getAllVolume() {
        return orders.stream().map(Order::getUnfilledVolume).collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return orders.size() == 0;
    }

    public Order getOrder(String orderId) {
        return orders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
    }

    private void saveSubOrder(String buyUserId, String sellUserId, String buyOrderId, String sellOrderId, BigDecimal price, BigDecimal volume) {
        log.info("add sub order price = {}, volume = {}", price.toPlainString(), volume.toPlainString());

    }

    public BigDecimal getVolume() {
        return volume;
    }


    private void cache(String orderId, BigDecimal volume) {
        Order o = getOrder(orderId);
        o.setUnfilledVolume(o.getUnfilledVolume().subtract(volume));
        BigDecimal cache = volumeCache.get(orderId);
        if (cache == null) {
            volumeCache.put(orderId, volume);
        } else {
            volumeCache.put(orderId, cache.add(volume));
        }
    }


}
