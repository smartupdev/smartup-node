package global.smartup.node.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.info.CTBuyInfo;
import global.smartup.node.eth.info.CTSellInfo;
import global.smartup.node.po.Market;
import global.smartup.node.po.User;
import global.smartup.node.service.*;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Api(description = "紧供测试")
@RestController
@RequestMapping("/api/test")
public class TestController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    private static final List<HashMap<String, String>> jobs = new ArrayList<>();

    private static final String DBJobPrefix = "job_order_";

    @Autowired
    private MarketService marketService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private KlineNodeService klineNodeService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private DictService dictService;

    @Autowired
    private IdGenerator idGenerator;


    @PostConstruct
    public void init() {
        loadDbJob();
        jobStart();
    }

    @ApiOperation(value = "买入CT", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：userAddress, marketId, sut, ct\n"
                  + "返回：是否成功")
    @RequestMapping("/buy")
    public Object buy(HttpServletRequest request, String userAddress, String marketId, BigDecimal sut, BigDecimal ct) {
        try {
            User user = userService.query(userAddress);
            if (user == null) {
                return Wrapper.alert("User not exist");
            }
            Market market = marketService.queryById(marketId);
            if (market == null) {
                return Wrapper.alert("Market not exist");
            }

            CTBuyInfo info = new CTBuyInfo();
            info.setTxHash(idGenerator.getHexStringId());
            info.setEventUserAddress(userAddress);
            info.setEventMarketAddress(market.getMarketAddress());
            info.setEventSUTOffer(sut);
            info.setEventSUT(sut);
            info.setEventCT(ct);
            info.setBlockTime(new Date());

            // save transaction
            tradeService.saveBuyTxByChain(info);

            // update ts
            transactionService.modTradeFinish(info.getTxHash(), PoConstant.TxStage.Success,
                    PoConstant.Transaction.Type.BuyCT, userAddress, market.getMarketId(), market.getMarketAddress(),
                    info.getEventSUT(), info.getEventCT(), info.getBlockTime());

            // update kline
            klineNodeService.updateNodeForBuyTxByChain(info);

            // update market data
            marketService.updateBuyTradeByChain(info);

            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "卖出CT", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：userAddress, marketId, sut, ct\n"
                  + "返回：是否成功")
    @RequestMapping("/sell")
    public Object sell(HttpServletRequest request, String userAddress, String marketId, BigDecimal sut, BigDecimal ct) {
        try {
            User user = userService.query(userAddress);
            if (user == null) {
                return Wrapper.alert("User not exist");
            }
            Market market = marketService.queryById(marketId);
            if (market == null) {
                return Wrapper.alert("Market not exist");
            }

            CTSellInfo info = new CTSellInfo();
            info.setTxHash(idGenerator.getHexStringId());
            info.setEventMarketAddress(market.getMarketAddress());
            info.setEventUserAddress(user.getUserAddress());
            info.setEventSUT(sut);
            info.setEventCT(ct);
            info.setBlockTime(new Date());

            // save transaction
            tradeService.saveSellTxByChain(info);

            // update ts
            transactionService.modTradeFinish(info.getTxHash(), PoConstant.TxStage.Success, PoConstant.Transaction.Type.SellCT,
                    info.getEventUserAddress(), market.getMarketId(), info.getEventMarketAddress(), info.getEventSUT(),
                    info.getEventCT(), info.getBlockTime());

            // update kline
            klineNodeService.updateNodeForSellTxByChain(info);

            // update market data
            marketService.updateSellTradeByChain(info);

            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "查看任务", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：无\n" +
                        "返回：obj = { list = [id, type(buy/sell), time, count, userAddress, marketId, sut, ct] } ")
    @RequestMapping("/job/list")
    public Object jobList(HttpServletRequest request) {
        try {
            return Wrapper.success(jobs);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "添加任务", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：type(buy/sell), time(eg. 2019-04-02 23:59:01), count(次数), userAddress, marketId, sut, ct\n" +
                        "返回：obj = { /api/test/job/list }")
    @RequestMapping("/job/add")
    public Object jobAdd(HttpServletRequest request, String type, String time, Integer count, String userAddress, String marketId, BigDecimal sut, BigDecimal ct) {
        try {
            if (jobs.size() >= 50) {
                return Wrapper.alert("Max job size is 50");
            }

            if (!"buy".equals(type) && !"sell".equals(type)) {
                return Wrapper.alert("Type error");
            }
            try {
                DateUtils.parseDate(time, "yyyy-MM-dd HH:mm:ss");
            } catch (Exception e) {
                return Wrapper.alert("Time error");
            }
            count = count == null ? 1 : count;
            User user = userService.query(userAddress);
            if (user == null) {
                return Wrapper.alert("User not exist");
            }
            Market market = marketService.queryById(marketId);
            if (market == null) {
                return Wrapper.alert("Market not exist");
            }

            HashMap<String, String> map = new HashMap<>();
            map.put("id", idGenerator.getHexStringId());
            map.put("type", type);
            map.put("time", time);
            map.put("count", String.valueOf(count));
            map.put("userAddress", userAddress);
            map.put("marketId", marketId);
            map.put("sut", sut.toPlainString());
            map.put("ct", ct.toPlainString());
            jobs.add(map);
            updateDbJob();
            return Wrapper.success(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "删除任务", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：id\n" +
                        "返回：是否成功")
    @RequestMapping("/job/del")
    public Object jobDel(HttpServletRequest request, String id) {
        try {
            Iterator<HashMap<String, String>> it = jobs.iterator();
            while (it.hasNext()) {
                HashMap<String, String> i = it.next();
                if (i.get("id").equals(id)) {
                    it.remove();
                    break;
                }
            }
            updateDbJob();
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    private void updateDbJob() {
        // del db
        for (int i = 0; i < 50; i++) {
            dictService.del(DBJobPrefix + i);
        }
        //save db
        for (int i = 0; i < jobs.size(); i++) {
            dictService.save(DBJobPrefix + i, JSON.toJSONString(jobs.get(i)));
        }
    }

    private void loadDbJob() {
        jobs.clear();
        for (int i = 0; i < 50; i++) {
            String s = dictService.query(DBJobPrefix + i);
            if (StringUtils.isNotBlank(s)) {
                HashMap map = JSON.parseObject(s, HashMap.class, Feature.UseBigDecimal);
                jobs.add(map);
            } else {
                return;
            }
        }
    }

    private void jobStart() {
        TestController c = this;
        Thread jobThread = new Thread(() -> {
            while (true) {
                List<String> delIds = new ArrayList<>();
                for (HashMap<String, String> job : jobs) {
                    String time = job.get("time");
                    Date d = null;
                    try {
                        d = DateUtils.parseDate(time, "yyyy-MM-dd HH:mm:ss");
                    } catch (ParseException e) {}
                    assert d != null;
                    if (System.currentTimeMillis() > d.getTime()) {
                        String type = job.get("type");
                        Integer count = Integer.valueOf(job.get("count"));
                        for (Integer i = 0; i < count; i++) {
                            if ("buy".equals(type)) {
                                c.buy(null, job.get("userAddress"), job.get("marketId"), new BigDecimal(job.get("sut")), new BigDecimal(job.get("ct")));

                            } else {
                                c.sell(null, job.get("userAddress"), job.get("marketId"), new BigDecimal(job.get("sut")), new BigDecimal(job.get("ct")));
                            }
                        }
                        delIds.add(job.get("id"));
                    }
                }
                Iterator<HashMap<String, String>> it = jobs.iterator();
                while (it.hasNext()) {
                    HashMap<String, String> i = it.next();
                    if (delIds.contains(i.get("id"))) {
                        it.remove();
                    }
                }
                c.updateDbJob();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        jobThread.start();
    }


}
