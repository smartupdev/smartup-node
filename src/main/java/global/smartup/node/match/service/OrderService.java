package global.smartup.node.match.service;

import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.TradeChildMapMapper;
import global.smartup.node.mapper.TradeChildMapper;
import global.smartup.node.mapper.TradeMapper;
import global.smartup.node.po.Trade;
import global.smartup.node.po.TradeChild;
import global.smartup.node.po.TradeChildMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    public static final Integer LoadPageSize = 500;

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private TradeChildMapper tradeChildMapper;

    @Autowired
    private TradeChildMapMapper tradeChildMapMapper;

    @Autowired
    private IdGenerator idGenerator;

    public List<Trade> queryTradePage(String marketId, Integer pageNumb) {
        return tradeMapper.selectPage(marketId, PoConstant.Trade.State.Trading, (pageNumb - 1) * LoadPageSize, LoadPageSize);
    }

    public List<TradeChild> queryTopChild(String marketId) {
        return tradeChildMapper.selectTop(marketId, 100);
    }

    public String saveOrder(String marketId, String userAddress, String type, BigDecimal entrustPrice, BigDecimal entrustVolume, BigDecimal fee, String sign) {
        String id = idGenerator.getHexStringId();
        Date current = new Date();
        Trade trade = new Trade();
        trade.setTradeId(id);
        trade.setMarketId(marketId);
        trade.setUserAddress(userAddress);
        trade.setType(type);
        trade.setState(PoConstant.Trade.State.Trading);
        trade.setEntrustPrice(entrustPrice);
        trade.setEntrustVolume(entrustVolume);
        trade.setFilledVolume(BigDecimal.ZERO);
        trade.setFee(fee);
        trade.setSign(sign);
        trade.setCreateTime(current);
        trade.setUpdateTime(current);
        tradeMapper.insert(trade);
        return id;
    }

    @Transactional
    public void saveChildAndUpdateParent(String marketId, String buyTradeId, String sellTradeId, BigDecimal price, BigDecimal volume) {
        String cid = idGenerator.getHexStringId();
        TradeChild child = new TradeChild();
        child.setChildId(cid);
        child.setMarketId(marketId);
        child.setPrice(price);
        child.setVolume(volume);
        child.setTxHash(null);
        child.setCreateTime(new Date());
        tradeChildMapper.insert(child);

        TradeChildMap buyMap = new TradeChildMap();
        buyMap.setChildId(cid).setTradeId(buyTradeId);
        tradeChildMapMapper.insert(buyMap);

        TradeChildMap sellMap = new TradeChildMap();
        sellMap.setChildId(cid).setTradeId(sellTradeId);
        tradeChildMapMapper.insert(sellMap);

        Trade buy = tradeMapper.selectByPrimaryKey(buyTradeId);
        buy.setFilledVolume(buy.getFilledVolume().add(volume));
        buy.setAvgPrice(calculateAvgPrice(buyTradeId));
        buy.setUpdateTime(new Date());
        if (buy.getEntrustVolume().compareTo(buy.getFilledVolume()) == 0) {
            buy.setState(PoConstant.Trade.State.Done);
        }
        tradeMapper.updateByPrimaryKey(buy);

        Trade sell = tradeMapper.selectByPrimaryKey(sellTradeId);
        sell.setFilledVolume(sell.getFilledVolume().add(volume));
        buy.setAvgPrice(calculateAvgPrice(sellTradeId));
        sell.setUpdateTime(new Date());
        if (sell.getEntrustVolume().compareTo(sell.getFilledVolume()) == 0) {
            sell.setState(PoConstant.Trade.State.Done);
        }
        tradeMapper.updateByPrimaryKey(sell);
    }

    public void updateOrderCancel(String tradeId) {
        Trade trade = tradeMapper.selectByPrimaryKey(tradeId);
        String state = PoConstant.Trade.State.CancelPart;
        if (trade.getFilledVolume().compareTo(BigDecimal.ZERO) == 0) {
            state = PoConstant.Trade.State.Cancel;
        }
        trade.setState(state);
        tradeMapper.updateByPrimaryKey(trade);
    }

    @Transactional
    public void updateCancelLeft(List<String> tradeIds) {
        Example example = new Example(Trade.class);
        example.createCriteria().andIn("tradeId", tradeIds);
        List<Trade> trades = tradeMapper.selectByExample(example);
        for (Trade trade : trades) {
            trade.setEntrustVolume(trade.getFilledVolume());
            trade.setState(PoConstant.Trade.State.Done);
            tradeMapper.updateByPrimaryKey(trade);
        }
    }

    private BigDecimal calculateAvgPrice(String tradeId) {
        List<TradeChild> children = queryChild(tradeId);
        if (children.size() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        for (TradeChild child : children) {
            volume = volume.add(child.getVolume());
            value = value.add(child.getVolume().multiply(child.getPrice()));
        }
        return value.divide(volume, 20, BigDecimal.ROUND_DOWN);
    }

    private List<TradeChild> queryChild(String tradeId) {
        TradeChildMap map = new TradeChildMap();
        map.setTradeId(tradeId);
        List<TradeChildMap> maps = tradeChildMapMapper.select(map);
        List<String> ids = maps.stream().map(TradeChildMap::getChildId).collect(Collectors.toList());
        Example example = new Example(TradeChild.class);
        example.createCriteria().andIn("childId", ids);
        return tradeChildMapper.selectByExample(example);
    }

}
