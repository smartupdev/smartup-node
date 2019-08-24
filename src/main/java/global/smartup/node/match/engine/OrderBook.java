package global.smartup.node.match.engine;

import global.smartup.node.match.bo.Order;
import global.smartup.node.match.common.OrderType;
import global.smartup.node.match.service.OrderService;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public class OrderBook {

    private static final Logger log = LoggerFactory.getLogger(OrderBook.class);

    private OrderService orderService;

    private MatchEngine engine;

    private OrderType type;

    private int scale = 4;

    private HashMap<String, Order> orderMap;

    private TreeMap<BigDecimal, Bucket> bucketMap;

    public OrderBook(MatchEngine engine, OrderType type, Integer scale) {
        this.engine = engine;
        this.type = type;
        this.orderService = engine.getOrderService();
        this.orderMap = new HashMap<>();
        if (scale != null) {
            this.scale = scale;
        }
        if (OrderType.Sell.equals(type)) {
            bucketMap = new TreeMap<>(BigDecimal::compareTo);
        } else if (OrderType.Buy.equals(type)) {
            bucketMap = new TreeMap<>((a, b) -> a.compareTo(b) * -1);
        } else {
            throw new IllegalArgumentException("OrderType " + type.name() + " error");
        }
    }

    public void loadOrder(Order order) {
        BigDecimal k = order.getEntrustPrice().setScale(scale, BigDecimal.ROUND_DOWN);
        Bucket bucket = bucketMap.get(k);
        if (bucket == null) {
            bucket = new Bucket(engine, this, k);
            bucketMap.put(k, bucket);
        }
        bucket.addOrder(order);
        orderMap.put(order.getOrderId(), order);
    }

    /**
     * 获取一个订单
     * @param orderId
     * @return
     */
    public Order getOrder(String orderId) {
        return orderMap.get(orderId);
    }

    /**
     * 添加挂单
     * @param order
     */
    public void makeOrder(Order order) {
        assert order != null;
        BigDecimal k = order.getEntrustPrice().setScale(scale, BigDecimal.ROUND_DOWN);
        Bucket bucket = bucketMap.get(k);
        if (bucket == null) {
            bucket = new Bucket(engine, this, k);
            bucketMap.put(k, bucket);
        }
        bucket.addOrder(order);
        orderMap.put(order.getOrderId(), order);
    }

    /**
     * 匹配计算
     * 计算当前价格可以匹配多少次交易，多少量
     * @param price 价格
     * @param volume 交易量
     * @return 匹配次数，成交量
     */
    public Integer takeCalculate(BigDecimal price, BigDecimal volume) {
        BigDecimal leftVolume = volume;
        Integer times = 0;
        Iterator<BigDecimal> iterator = bucketMap.keySet().iterator();
        while (iterator.hasNext()) {
            BigDecimal k = iterator.next();
            if (leftVolume.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (OrderType.Sell.equals(this.type)) {
                if (price.compareTo(k) < 0) {
                    break;
                }
            } else {
                if (price.compareTo(k) > 0) {
                    break;
                }
            }
            Bucket bucket = bucketMap.get(k);
            Pair<Integer, BigDecimal> pair = bucket.takeCalculate(times, leftVolume);
            leftVolume = pair.getRight();
            times = pair.getLeft();
        }
        return times;
    }

    /**
     * 匹配一批订单
     * 针对sellOrder
     * @param orders
     *   price: xx
     *   volume: xx
     * return times in map
     */
    public void takeCalculate(List<Map<String, Object>> orders) {
        assert OrderType.Buy.equals(type);

        // 排序订单，先处理价格高的
        orders.sort((m1, m2) -> {
            BigDecimal p1 = (BigDecimal) m1.get("price");
            BigDecimal p2 = (BigDecimal) m2.get("price");
            return p1.compareTo(p2) * -1;
        });

        // 计算匹配次数
        // minK 标识最小匹配到的价格，用于后续清除缓存
        BigDecimal minK = null;
        for (int i = 0; i < orders.size(); i++) {
            Map<String, Object> map = orders.get(i);
            BigDecimal price = (BigDecimal) map.get("price");
            BigDecimal volume = (BigDecimal) map.get("volume");
            Integer times = 0;

            Iterator<BigDecimal> iterator = bucketMap.keySet().iterator();
            while (iterator.hasNext()) {
                BigDecimal k = iterator.next();
                if (minK == null) {
                    minK = k;
                } else {
                    if (k.compareTo(minK) < 0) {
                        minK = k;
                    }
                }
                if (volume.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                if (price.compareTo(k) > 0) {
                    break;
                }
                Bucket bucket = bucketMap.get(k);
                Pair<Integer, BigDecimal> pair = bucket.takeCalculateWithCache(times, volume);
                times = pair.getLeft();
                volume = pair.getRight();
            }
            map.put("times", times);
        }

        // 清除缓存
        Iterator<BigDecimal> iterator = bucketMap.keySet().iterator();
        while (iterator.hasNext()) {
            BigDecimal k = iterator.next();
            if (minK.compareTo(k) <= 0) {
                bucketMap.get(k).clearCache();
            } else {
                break;
            }
        }
    }

    /**
     * 匹配吃单
     * @param order
     */
    public void take(Order order) {
        // BigDecimal leftVolume = volume;
        Iterator<BigDecimal> iterator = bucketMap.keySet().iterator();
        while (iterator.hasNext()) {
            BigDecimal k = iterator.next();
            if (OrderType.Sell.equals(this.type)) {
                if (order.getEntrustPrice().compareTo(k) < 0) {
                    break;
                }
            } else {
                if (order.getEntrustPrice().compareTo(k) > 0) {
                    break;
                }
            }
            if (order.getTimes().compareTo(0) <= 0) {
                break;
            }
            if (order.getUnfilledVolume().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            Bucket bucket = bucketMap.get(k);
            Integer takeCount = bucket.take(order);
            if (takeCount.compareTo(0) > 0) {
                engine.updateCurrentPrice(k);
            }
            if (bucket.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public void clearBucketOrder(Order order) {
        BigDecimal k = order.getEntrustPrice().setScale(scale, BigDecimal.ROUND_DOWN);
        Bucket b = bucketMap.get(k);
        if (order.getUnfilledVolume().compareTo(BigDecimal.ZERO) <= 0) {
            // remove order
            b.removeOrder(order.getOrderId());
            orderMap.remove(order.getOrderId());
            if (b.isEmpty()) {
                bucketMap.remove(k);
            }
        }
        // update bucket
        b.updateVolume();
    }

    /**
     * 将订单中未成交的部分移除
     * @param orderIds
     */
    public void updateCancelLeft(List<String> orderIds) {
        for (String id : orderIds) {
            Order o = getOrder(id);
            assert o != null;
            o.setEntrustVolume(o.getEntrustVolume().subtract(o.getUnfilledVolume()));
            o.setUnfilledVolume(BigDecimal.ZERO);
            clearBucketOrder(o);
            orderService.updateCancelLeft(orderIds);
        }
    }

    public List<Map<String, BigDecimal>> queryOrderBook(Integer top) {
        top = (top == null || top <= 0) ? top = 10 : top;
        List<Map<String, BigDecimal>> ret = new ArrayList<>();
        Iterator<BigDecimal> iterator = bucketMap.keySet().iterator();
        for (Integer i = 0; i < top; i++) {
            if (!iterator.hasNext()) {
                break;
            }
            BigDecimal k = iterator.next();
            Bucket bucket = bucketMap.get(k);
            if (bucket == null) {
                continue;
            }
            Map<String, BigDecimal> map = new HashMap<>();
            map.put("price", k);
            map.put("volume", bucket.getVolume());
            ret.add(map);
        }
        return ret;
    }

    public Order cancelOrder(String userId, String orderId) {
        Order o = getOrder(orderId);
        if (o == null) {
            return null;
        }
        if (!o.getOrderId().equals(userId)) {
            return null;
        }
        BigDecimal k = o.getEntrustPrice().setScale(scale, BigDecimal.ROUND_DOWN);
        Bucket bucket = bucketMap.get(k);
        assert bucket != null;
        bucket.removeOrder(orderId);
        orderMap.remove(orderId);
        // 更新订单取消
        orderService.updateOrderCancel(orderId);
        return o;
    }

    public void removeOrderMap(String orderId) {
        this.orderMap.remove(orderId);
    }

}
