package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.constant.RedisKey;
import global.smartup.node.eth.info.CTBuyInfo;
import global.smartup.node.eth.info.CTSellInfo;
import global.smartup.node.mapper.KlineNodeMapper;
import global.smartup.node.po.KlineNode;
import global.smartup.node.util.Common;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.xml.soap.Node;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class KlineNodeService {

    private static final Logger log = LoggerFactory.getLogger(KlineNodeService.class);

    @Autowired
    private KlineNodeMapper klineNodeMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TransactionService transactionService;


    public synchronized KlineNode createNode(String marketAddress, String segment, Date date,
                                             BigDecimal start, BigDecimal end, BigDecimal amount,  Long count) {
        String timeId = Common.getTimeId(segment, date);
        KlineNode exits = queryNodeByTimeId(marketAddress, segment, timeId);
        if (exits != null) {
            return null;
        }
        KlineNode node = new KlineNode();
        node.setMarketAddress(marketAddress);
        node.setStart(start);
        node.setEnd(end);
        node.setHigh(end);
        node.setLow(end);
        node.setAmount(amount);
        node.setCount(count);
        node.setTimeId(timeId);
        node.setTime(Common.fillZero(segment, date));
        node.setSegment(segment);
        klineNodeMapper.insert(node);
        return node;
    }

    public void updateNodeForBuyTxByChain(CTBuyInfo info) {
        if (info == null) {
            return;
        }
        String marketAddress = info.getEventMarketAddress();
        BigDecimal sut = info.getEventSUT();
        BigDecimal ct = info.getEventCT();
        Date time = info.getBlockTime();
        updateNodeByChain(marketAddress, sut, ct, time);
    }

    public void updateNodeForSellTxByChain(CTSellInfo info) {
        if (info == null) {
            return;
        }
        String marketAddress = info.getEventMarketAddress();
        BigDecimal sut = info.getEventSUT();
        BigDecimal ct = info.getEventCT();
        Date time = info.getBlockTime();
        updateNodeByChain(marketAddress, sut, ct, time);
    }

    public void updateNodeByChain(String marketAddress, BigDecimal sut, BigDecimal ct, Date blockTime) {
        // cal price
        BigDecimal price = sut.divide(ct, 20, RoundingMode.DOWN);

        // update every segment node
        for (String segment : PoConstant.KLineNode.Segment.All) {
            String timeId = Common.getTimeId(segment, blockTime);
            KlineNode node = queryNodeByTimeId(marketAddress, segment, timeId);

            if (node == null) {
                BigDecimal start = queryLastPrice(marketAddress, segment, blockTime);
                start = start != null ? start : price;
                node = createNode(marketAddress, segment, blockTime, start, price, sut, 1L);
            } else {
                if (price.compareTo(node.getHigh()) > 0) {
                    node.setHigh(price);
                }
                if (price.compareTo(node.getLow()) < 0) {
                    node.setLow(price);
                }

                // 判断是否是一个阶段的最后一笔的交易
                boolean isLastTrade = transactionService.isLastTradeTransactionInSegment(marketAddress, blockTime, segment);

                // 如果是最后一笔才能修改end，因为有可能后来插入的交易
                if (isLastTrade) {
                    node.setEnd(price);
                }
                node.setAmount(node.getAmount().add(sut));
                node.setCount(node.getCount() + 1);
                klineNodeMapper.updateByPrimaryKey(node);

                // remove cache
                removeCache(marketAddress, segment, timeId);

            }

            // update node already existed
            KlineNode next = queryNextNode(marketAddress, segment, blockTime);
            if (next != null) {
                updateFixExistedNode(node.getEnd(), next);
            }

        }
    }

    // 因为插入了新的交易，end price 改变，需要修复已经存在kline
    public void updateFixExistedNode(BigDecimal end, KlineNode next) {
        if (end.compareTo(next.getStart()) == 0) {
            return;
        }
        next.setStart(end);
        if (next.getCount() <= 0) {
            next.setEnd(end);
            next.setHigh(end);
            next.setLow(end);
        } else {
            if (end.compareTo(next.getHigh()) > 0) {
                next.setHigh(end);
            }
            if (end.compareTo(next.getLow()) < 0) {
                next.setLow(end);
            }
        }
        klineNodeMapper.updateByPrimaryKey(next);

        // remove cache
        removeCache(next.getMarketAddress(), next.getSegment(), next.getTimeId());

        // loop
        KlineNode next2 = queryNextNode(next.getMarketAddress(), next.getSegment(), next.getTime());
        if (next2 != null) {
            updateFixExistedNode(next.getEnd(), next2);
        }
    }

    public void keepNodeContinue(String marketAddress, String segment) {
        Date current = new Date();
        String timeId = Common.getTimeId(segment, current);
        KlineNode node = queryNodeByTimeId(marketAddress, segment, timeId);
        if (node == null) {
            KlineNode last = queryLastNode(marketAddress, segment, current);
            if (last != null) {
                createNode(marketAddress, segment, current, last.getEnd(), last.getEnd(), BigDecimal.ZERO, 0L);

                // remove cache
                removeCache(marketAddress, segment, timeId);
            } else {
                // 判断k线出现了断裂
                if (hasNode(marketAddress)) {
                    KlineNode breakNode = queryBreakNode(marketAddress, segment);
                    if (breakNode != null) {
                        keepBreakNode(breakNode);
                    }
                }
            }
        }
    }

    public void keepBreakNode(KlineNode node) {
        if (node == null) {
            return;
        }
        Date nextTime = Common.getNextTime(node.getSegment(), node.getTime());
        if (nextTime.getTime() > System.currentTimeMillis()) {
            return;
        }
        KlineNode next = queryNextNode(node.getMarketAddress(), node.getSegment(), node.getTime());
        if (next != null) {
            return;
        }
        next = new KlineNode();
        next.setMarketAddress(node.getMarketAddress());
        next.setSegment(node.getSegment());
        next.setHigh(node.getEnd());
        next.setLow(node.getEnd());
        next.setStart(node.getEnd());
        next.setEnd(node.getEnd());
        next.setCount(0L);
        next.setAmount(BigDecimal.ZERO);
        next.setTimeId(Common.getNextTimeId(node.getSegment(), node.getTime()));
        next.setTime(nextTime);
        klineNodeMapper.insert(next);

        // remove cache
        removeCache(next.getMarketAddress(), next.getSegment(), next.getTimeId());

        // loop
        keepBreakNode(next);
    }

    public boolean hasNode(String marketAddress) {
        KlineNode cdt = new KlineNode();
        cdt.setMarketAddress(marketAddress);
        PageHelper.startPage(1, 1, false);
        List<KlineNode> list = klineNodeMapper.select(cdt);
        return list.size() > 0;
    }

    public void removeCache(String marketAddress, String segment, String timeId) {
        String key = RedisKey.KlinePrefix + marketAddress + ":" + segment + ":" + timeId;
        redisTemplate.delete(key);
    }

    public KlineNode queryBreakNode(String marketAddress, String segment) {
        Example example = new Example(KlineNode.class);
        example.createCriteria()
                .andEqualTo("marketAddress", marketAddress)
                .andEqualTo("segment", segment);
        example.orderBy("time").desc();
        PageHelper.startPage(1, 1, false);
        List<KlineNode> list = klineNodeMapper.selectByExample(example);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map<String, KlineNode> querySegmentNodes(String marketAddress, Date time) {
        Map<String, KlineNode> map = new HashMap<>();
        for (String segment : PoConstant.KLineNode.Segment.All) {
            String timeId = Common.getTimeId(segment, time);
            KlineNode node = queryNodeByTimeId(marketAddress, segment, timeId);
            map.put(segment, node);
        }
        return map;
    }

    public BigDecimal queryLatelyVolume(String marketAddress, int hour) {
        BigDecimal ret = BigDecimal.ZERO;
        Date current = Common.fillZero(PoConstant.KLineNode.Segment.Hour, new Date());
        Date start = Common.getSomeHoursAgo(current, hour);
        List<KlineNode> list = queryNodes(marketAddress, PoConstant.KLineNode.Segment.Hour, start, current);
        for (KlineNode node : list) {
            ret = ret.add(node.getAmount());
        }
        return ret;
    }

    public BigDecimal queryLatelyChange(String marketAddress, BigDecimal price, int hour) {
        Date current = new Date();
        Date ago = Common.getSomeHoursAgo(current, hour);
        String aId = Common.getTimeId(PoConstant.KLineNode.Segment.Hour, ago);
        KlineNode aNode = queryNodeByTimeId(marketAddress, PoConstant.KLineNode.Segment.Hour, aId);
        if (aNode == null) {
            return null;
        }
        BigDecimal change = price.subtract(aNode.getEnd()).divide(aNode.getEnd(), 20, BigDecimal.ROUND_DOWN);
        return change;
    }

    public KlineNode queryNodeByTimeId(String marketAddress, String segment, String timeId) {
        if (StringUtils.isAnyBlank(marketAddress, segment, timeId)) {
            return null;
        }
        KlineNode cdt = new KlineNode();
        cdt.setMarketAddress(marketAddress);
        cdt.setSegment(segment);
        cdt.setTimeId(timeId);
        return klineNodeMapper.selectOne(cdt);
    }

    public KlineNode queryNextNode(String marketAddress, String segment, Date time) {
        if (StringUtils.isAnyBlank(marketAddress, segment) || time == null) {
            return null;
        }
        String lastId = Common.getNextTimeId(segment, time);
        return queryNodeByTimeId(marketAddress, segment, lastId);
    }

    public KlineNode queryLastNode(String marketAddress, String segment, Date time) {
        if (StringUtils.isAnyBlank(marketAddress, segment) || time == null) {
            return null;
        }
        String lastId = Common.getLastTimeId(segment, time);
        return queryNodeByTimeId(marketAddress, segment, lastId);
    }

    public BigDecimal queryCurrentPrice(String marketAddress, String segment, Date time) {
        String timeId = Common.getTimeId(segment, time);
        if (timeId == null) {
            return null;
        }
        KlineNode node = queryNodeByTimeId(marketAddress, segment, timeId);
        if (node == null) {
            return null;
        }
        return node.getEnd();
    }

    public BigDecimal queryLastPrice(String marketAddress, String segment, Date time) {
        KlineNode node = queryLastNode(marketAddress, segment, time);
        if (node == null) {
            return null;
        } else {
            return node.getEnd();
        }
    }

    public List<KlineNode> queryNodes(String marketAddress, String segment, Date start, Date end) {
        List<KlineNode> list = new ArrayList<>();
        if (StringUtils.isAnyBlank(marketAddress, segment) || start == null || end == null) {
            return list;
        }
        Example example = new Example(KlineNode.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("marketAddress", marketAddress)
                .andEqualTo("segment", segment)
                .andGreaterThanOrEqualTo("time", start)
                .andLessThanOrEqualTo("time", end);
        return klineNodeMapper.selectByExample(example);
    }

    public List<KlineNode> queryNodes(String marketAddress, String segment, String start, String end) {
        List<KlineNode> list = new ArrayList<>();
        Date s, e;
        try {
            if (StringUtils.isBlank(start)) {
                s = Common.getSomeDaysAgo(new Date(), 7);
            } else {
                s = DateUtils.parseDate(start, "yyyy-MM-dd");
            }
            if (StringUtils.isBlank(end)) {
                e = new Date();
            } else {
                e = DateUtils.parseDate(end, "yyyy-MM-dd");
            }
        } catch (ParseException exception) {
            return list;
        }
        return queryNodes(marketAddress, segment, Common.getDayStart(s), e);
    }

    public List<KlineNode> queryCacheNodes(String marketAddress, String segment, String start, String end) {
        List<KlineNode> list = new ArrayList<>();
        List<String> ids = Common.getTimeIdInRange(segment, start, end);
        ids.forEach(id -> {
            KlineNode n = queryCachedNode(marketAddress, segment, id);
            if (n != null) {
                list.add(n);
            }
        });
        return list;
    }

    public KlineNode queryCachedNode(String marketAddress, String segment, String timeId) {
        KlineNode node;
        if (StringUtils.isAnyBlank(marketAddress, timeId)) {
            return null;
        }
        Boolean isFuture = Common.isFuture(segment, timeId);
        if (isFuture == null || isFuture) {
            return null;
        }
        String redisKey = RedisKey.KlinePrefix + marketAddress + ":" + segment + ":" + timeId;
        Object val = redisTemplate.opsForValue().get(redisKey);
        if (val == null) {
            node = queryNodeByTimeId(marketAddress, segment, timeId);
            if (node == null) {
                redisTemplate.opsForValue().set(redisKey, RedisKey.NoDataFlag, RedisKey.KlineExpire, TimeUnit.MILLISECONDS);
                return null;
            } else {
                redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(node), RedisKey.KlineExpire, TimeUnit.MILLISECONDS);
                return node;
            }
        }
        if (RedisKey.NoDataFlag.equals(val.toString())) {
            return null;
        } else {
            return JSON.parseObject(val.toString(), KlineNode.class);
        }
    }

    public List<BigDecimal> querySevenDayNode(String marketAddress) {
        List<BigDecimal> ret = new ArrayList<>();
        List<String> nodeId = Common.getSevenDay6HourNode();
        nodeId.forEach(n -> {
            KlineNode node = queryCachedNode(marketAddress, PoConstant.KLineNode.Segment.Hour, n);
            if (node != null) {
                ret.add(node.getEnd());
            }
        });
        return ret;
    }

}

