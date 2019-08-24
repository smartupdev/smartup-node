package global.smartup.node.match;

import com.alibaba.fastjson.JSON;
import global.smartup.node.match.bo.Order;
import global.smartup.node.match.engine.MatchEngine;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineTest {

    @Test
    public void take() {

        // MatchEngine engine = new MatchEngine("1");
        //
        // engine.makeSellOrder("1", BigDecimal.valueOf(100.0003), BigDecimal.valueOf(200), 0);
        // engine.makeSellOrder("1", BigDecimal.valueOf(100.0002), BigDecimal.valueOf(200), 0);
        // engine.makeSellOrder("1", BigDecimal.valueOf(100.0001), BigDecimal.valueOf(200), 0);
        //
        // Map<String, Object> ret1 = engine.makeBuyOrder("2", BigDecimal.valueOf(100.0001), BigDecimal.valueOf(100), 1);
        // Map<String, Object> ret2 = engine.makeBuyOrder("2", BigDecimal.valueOf(100.0002), BigDecimal.valueOf(200), 2);
        //
        // System.out.println("");

    }

    @Test
    public void updateSellOrder() {
        // MatchEngine engine = new MatchEngine("1");
        //
        // Map<String, Object> m1 = engine.makeSellOrder("1", BigDecimal.valueOf(100.0007), BigDecimal.valueOf(200), 0);
        // Map<String, Object> m2 = engine.makeSellOrder("1", BigDecimal.valueOf(100.0006), BigDecimal.valueOf(200), 0);
        //
        // engine.makeBuyOrder("2", BigDecimal.valueOf(100.0004), BigDecimal.valueOf(200), 0);
        // engine.makeBuyOrder("2", BigDecimal.valueOf(100.0003), BigDecimal.valueOf(200), 0);
        //
        // List<String> orderIds = new ArrayList<>();
        // orderIds.add(((Order) m1.get("order")).getOrderId());
        // orderIds.add(((Order) m2.get("order")).getOrderId());
        //
        // List<Map<String, Object>> newOrders = new ArrayList<>();
        // Map<String, Object> o1 = new HashMap<>();
        // o1.put("price", BigDecimal.valueOf(100.0004));
        // o1.put("volume", BigDecimal.valueOf(200));
        // newOrders.add(o1);
        //
        // Map<String, Object> o2 = new HashMap<>();
        // o2.put("price", BigDecimal.valueOf(100.0003));
        // o2.put("volume", BigDecimal.valueOf(200));
        // newOrders.add(o2);
        //
        // Map<String, Object> ret = engine.updateSellOrder("1", orderIds, newOrders, 2);
        //
        // System.out.println(JSON.toJSONString(ret));
        // System.out.println("over");
    }

}
