package global.smartup.node.service;

import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.info.BuyCTInfo;
import global.smartup.node.eth.info.SellCTInfo;
import global.smartup.node.mapper.KlineNodeMapper;
import global.smartup.node.po.KlineNode;
import global.smartup.node.po.Trade;
import global.smartup.node.util.Common;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;

@Service
public class KlineNodeService {

    private static final Logger log = LoggerFactory.getLogger(KlineNodeService.class);

    @Autowired
    private KlineNodeMapper klineNodeMapper;

    public synchronized KlineNode createNode(String marketAddress, String segment, Date date, BigDecimal start, BigDecimal sut, Long count) {
        String timeId = Common.getTimeId(segment, date);
        KlineNode exits = queryNodeByTimeId(marketAddress, segment, timeId);
        if (exits != null) {
            return null;
        }
        KlineNode node = new KlineNode();
        node.setMarketAddress(marketAddress);
        node.setStart(start);
        node.setEnd(sut);
        node.setHigh(sut);
        node.setLow(sut);
        node.setAmount(sut);
        node.setCount(count);
        node.setTimeId(timeId);
        node.setTime(Common.fillZero(segment, date));
        node.setSegment(segment);
        klineNodeMapper.insert(node);
        return node;
    }

    public void updateNodeForBuyTxByChain(BuyCTInfo info) {
        if (info == null) {
            return;
        }
        String marketAddress = info.getEventMarketAddress();
        BigDecimal sut = info.getEventSUT();
        BigDecimal ct = info.getInputCT();
        Date time = info.getBlockTime();
        updateNodeByChain(marketAddress, sut, ct, time);
    }

    public void updateNodeForSellTxByChain(SellCTInfo info) {
        if (info == null) {
            return;
        }
        String marketAddress = info.getEventMarketAddress();
        BigDecimal sut = info.getEventSUT();
        BigDecimal ct = info.getInputCT();
        Date time = info.getBlockTime();
        updateNodeByChain(marketAddress, sut, ct, time);
    }

    public void updateNodeByChain(String marketAddress, BigDecimal sut, BigDecimal ct, Date time) {
        // cal price
        BigDecimal price = sut.divide(ct, 20, RoundingMode.DOWN);

        // update every segment node
        for (String segment : PoConstant.KLineNode.Segment.All) {
            String timeId = Common.getTimeId(segment, time);
            KlineNode node = queryNodeByTimeId(marketAddress, segment, timeId);
            if (node == null) {
                BigDecimal start = queryLastPrice(marketAddress, segment, time);
                start = start != null ? start : price;
                createNode(marketAddress, segment, time, start, price, 1L);
            } else {
                if (price.compareTo(node.getHigh()) > 0) {
                    node.setHigh(price);
                }
                if (price.compareTo(node.getLow()) < 0) {
                    node.setLow(price);
                }
                node.setEnd(price);
                node.setAmount(node.getAmount().add(sut));
                node.setCount(node.getCount() + 1);
                klineNodeMapper.updateByPrimaryKey(node);
            }
        }
    }

    public void keepNodeContinue(String marketAddress, String segment) {
        Date current = new Date();
        String timeId = Common.getTimeId(segment, current);
        KlineNode node = queryNodeByTimeId(marketAddress, segment, timeId);
        if (node == null) {
            KlineNode last = queryLastNode(marketAddress, segment, current);
            if (last != null) {
                createNode(marketAddress, segment, current, last.getEnd(), last.getEnd(), 0L);
            }
        }
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

    public KlineNode queryLastNode(String marketAddress, String segment, Date time) {
        if (StringUtils.isAnyBlank(marketAddress, segment) || time == null) {
            return null;
        }
        String lastId = Common.getLastTimeId(segment, time);
        return queryNodeByTimeId(marketAddress, segment, lastId);
    }

    public BigDecimal queryLastPrice(String marketAddress, String segment, Date time) {
        KlineNode node = queryLastNode(marketAddress, segment, time);
        if (node == null) {
            return null;
        } else {
            return node.getEnd();
        }
    }

    public List<KlineNode> queryRangeNodes(String marketAddress, String segment, String start, String end) {
        List<KlineNode> list = new ArrayList<>();
        Date s, e;
        try {
            s = DateUtils.parseDate(start, "yyyy-MM-dd");
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

    public List<KlineNode> queryNodes(String marketAddress, String segment, Date start, Date end) {
        List<KlineNode> list = new ArrayList<>();
        if (StringUtils.isAnyBlank(marketAddress, segment) || start == null) {
            return list;
        }
        if (end == null) {
            end = Common.getDayEnd(new Date());
        }
        Example example = new Example(KlineNode.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("marketAddress", marketAddress)
                .andEqualTo("segment", segment)
                .andGreaterThanOrEqualTo("time", start)
                .andLessThanOrEqualTo("time", end);
        return klineNodeMapper.selectByExample(example);
    }

}
