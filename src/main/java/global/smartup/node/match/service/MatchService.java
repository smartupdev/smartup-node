package global.smartup.node.match.service;

import global.smartup.node.constant.PoConstant;
import global.smartup.node.match.bo.Order;
import global.smartup.node.match.common.Const;
import global.smartup.node.match.common.OrderType;
import global.smartup.node.match.engine.MatchEngine;
import global.smartup.node.po.Market;
import global.smartup.node.po.Trade;
import global.smartup.node.po.TradeChild;
import global.smartup.node.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private HashMap<String, MatchEngine> matchEngines;

    @Autowired
    private MMarketService marketService;

    @Autowired
    private MOrderService orderService;

    @PostConstruct
    public void initEngine() {
        log.info("Match engine init start");
        matchEngines = new HashMap<>();
        List<Market> markets =  marketService.loadAllMarket();
        markets.forEach(m -> {
            String id = m.getMarketId();
            MatchEngine engine = new MatchEngine(orderService, id, m.getMarketAddress());
            matchEngines.put(id, engine);
        });
        loadOrder();
    }

    private void loadOrder() {
        log.info("Match engine load order start");
        List<String> idList = new ArrayList<>(matchEngines.keySet());
        for (int i = 0; i < idList.size(); i++) {
            String id = idList.get(i);
            log.info("Load orders of market id=[{}] {}/{}", id, i + 1, idList.size());
            MatchEngine engine = matchEngines.get(id);

            // load order
            Integer pageNumb = 1;
            while (true) {
                List<Trade> list = orderService.queryTradePage(id, pageNumb);
                for (Trade t : list) {
                    Order o = new Order();
                    OrderType type;
                    if (PoConstant.Trade.Type.Buy.equals(t.getType())) {
                        type = OrderType.Buy;
                    } else if (PoConstant.Trade.Type.Sell.equals(t.getType())) {
                        type = OrderType.Sell;
                    } else {
                        continue;
                    }
                    o.setOrderId(t.getTradeId())
                        .setType(type)
                        .setUserAddress(t.getUserAddress())
                        .setEntrustPrice(t.getEntrustPrice())
                        .setEntrustVolume(t.getEntrustVolume())
                        .setUnfilledVolume(t.getEntrustVolume().subtract(t.getFilledVolume()));
                    engine.loadOrder(o);
                }
                if (list.isEmpty()) {
                    break;
                }
                pageNumb++;
            }

            // load order child
            List<TradeChild> children = orderService.queryTopChild(id);
            children.forEach(c -> {
                engine.loadLatelyOrder(c.getCreateTime(), c.getPrice(), c.getVolume());
            });

            // set current price
            if (children.size() >= 1) {
                engine.updateCurrentPrice(children.get(0).getPrice());
            }
            if (children.size() >= 2) {
                engine.updateCurrentPrice(children.get(1).getPrice());
            }

            engine.ready();
        }
        log.info("Match engine load order over");
    }

    public void loadFirstStageOrder(String marketId, Date time, BigDecimal price, BigDecimal volume) {
        MatchEngine engine = matchEngines.get(marketId);
        if (engine == null) {
            return;
        }
        engine.loadLatelyOrder(time, price, volume);
    }

    // 用户创建新市场
    public void addNewMarket(String marketId, String marketAddress) {
        MatchEngine engine = new MatchEngine(orderService, marketId, marketAddress);
        matchEngines.put(marketId, engine);
        engine.ready();
    }

    public Map<String, Object> queryMatchTime(String marketId, String type, BigDecimal price, BigDecimal volume) {
        MatchEngine engine = matchEngines.get(marketId);
        if (isNotReady(engine)) {
            return notReady();
        }
        Map<String, Object> ret = new HashMap<>();
        Integer times = engine.queryMatchTimes(type, price, volume);
        ret.put("code", Const.Success);
        ret.put("obj", MapBuilder.create().put("times", times).build());
        return ret;
    }

    public Map<String, Object> addBuyOrder(
        String marketId, String userId, BigDecimal price, BigDecimal volume, Integer times, Long gasPrice, Long gasLimit,
        Long timestamp, String makeSign, String takeSign
    ) {
        MatchEngine engine = matchEngines.get(marketId);
        if (isNotReady(engine)) {
            return notReady();
        }
        return engine.makeBuyOrder(userId, price, volume, times, gasPrice, gasLimit, timestamp, makeSign, takeSign);
    }

    public Map<String, Object> addSellOrder(
        String marketId, String userId, BigDecimal price, BigDecimal volume, Long timestamp, String sign
    ) {
        MatchEngine engine = matchEngines.get(marketId);
        if (isNotReady(engine)) {
            return notReady();
        }
        return engine.makeSellOrder(userId, price, volume, timestamp, sign);
    }

    public Map<String, Object> queryMatchTimeForUpdate(
        String marketId, String userId,
        List<String> cancelOrderIds,
        List<String> lockOrderIds,
        List<Map<String, Object>> newOrders
    ) {
        MatchEngine engine = matchEngines.get(marketId);
        if (isNotReady(engine)) {
            return notReady();
        }
        return engine.queryMatchTimesForUpdate(userId, cancelOrderIds, lockOrderIds, newOrders);
    }

    public Map<String, Object> updateSellOrder(
        String marketId, String userId,
        List<String> cancelOrderIds,
        List<String> lockOrderIds,
        List<Map<String, Object>> newOrders
    ) {
        MatchEngine engine = matchEngines.get(marketId);
        if (isNotReady(engine)) {
            return notReady();
        }
        return engine.updateSellOrder(userId, cancelOrderIds, lockOrderIds, newOrders);
    }

    public Map<String, Object> cancelBuyOrder(String marketId, String orderId) {
        MatchEngine engine = matchEngines.get(marketId);
        if (isNotReady(engine)) {
            return notReady();
        }
        boolean success = engine.cancelBuyOrder(orderId);
        Map<String, Object> ret = new HashMap<>();
        if (success) {
            ret.put("code", Const.Success);
        } else {
            ret.put("code", Const.OrderAlreadyDone);
        }
        return ret;
    }

    public Map<String, Object> queryOrderBook(String marketId, Integer topOfBook, Integer topOfOrder) {
        MatchEngine engine = matchEngines.get(marketId);
        if (isNotReady(engine)) {
            return notReady();
        }
        return engine.queryOrderBook(topOfBook, topOfOrder);
    }

    private boolean isNotReady(MatchEngine engine) {
        if (engine == null) {
            return true;
        }
        return !engine.isReady();
    }

    private Map<String, Object> notReady() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", Const.EngineNotReady);
        return map;
    }

}
