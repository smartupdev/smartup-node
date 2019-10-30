package global.smartup.node.match.service;

import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.*;
import global.smartup.node.match.bo.OrderChild;
import global.smartup.node.match.common.OrderType;
import global.smartup.node.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MOrderService {

    public static final Integer LoadPageSize = 500;

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private TradeChildMapper tradeChildMapper;

    @Autowired
    private TradeChildMapMapper tradeChildMapMapper;

    @Autowired
    private CTAccountMapper ctAccountMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private TakePlanMapper takePlanMapper;

    @Autowired
    private IdGenerator idGenerator;

    public List<Trade> queryTradePage(String marketId, Integer pageNumb) {
        return tradeMapper.selectPage(marketId, PoConstant.Trade.State.Trading, (pageNumb - 1) * LoadPageSize, LoadPageSize);
    }

    public List<TradeChild> queryTopChild(String marketId) {
        return tradeChildMapper.selectTop(marketId, 100);
    }

    @Transactional
    public Trade saveOrder(String marketId, String marketAddress, String userAddress, String type,
                           BigDecimal entrustPrice, BigDecimal entrustVolume, BigDecimal fee, Long timestamp, String makeSign) {
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
        trade.setTimestamp(timestamp);
        trade.setSign(makeSign);
        trade.setCreateTime(current);
        trade.setUpdateTime(current);
        tradeMapper.insert(trade);

        if (OrderType.Sell.getValue().equals(type)) {
            // 出售ct不需要锁定，ct一直是在出售状态
        } else {
            // lock sut
            UserAccount userAccount = userAccountMapper.selectByPrimaryKey(userAddress);
            BigDecimal lockSut = entrustVolume.multiply(entrustPrice);
            userAccount.setSut(userAccount.getSut().subtract(lockSut));
            userAccount.setSutLock(userAccount.getSutLock().add(lockSut));
            userAccountMapper.updateByPrimaryKey(userAccount);
        }
        return trade;
    }

    @Transactional
    public List<TradeChild> saveChildUpdateTrade(String marketId, String takePlanId, List<OrderChild> children) {
        List<TradeChild> tradeChildren = new ArrayList<>();
        for (OrderChild oc : children) {
            // save child
            String cid = idGenerator.getHexStringId();
            TradeChild child = new TradeChild();
            child.setChildId(cid);
            child.setMarketId(marketId);
            child.setTakePlanId(takePlanId);
            child.setPrice(oc.getPrice());
            child.setVolume(oc.getVolume());
            child.setTxHash(null);
            child.setCreateTime(new Date());
            tradeChildMapper.insert(child);
            tradeChildren.add(child);

            TradeChildMap buyMap = new TradeChildMap();
            buyMap.setChildId(cid).setTradeId(oc.getBuyOrderId());
            tradeChildMapMapper.insert(buyMap);

            TradeChildMap sellMap = new TradeChildMap();
            sellMap.setChildId(cid).setTradeId(oc.getSellOrderId());
            tradeChildMapMapper.insert(sellMap);

            // update trade
            Trade buy = tradeMapper.selectByPrimaryKey(oc.getBuyOrderId());
            buy.setFilledVolume(buy.getFilledVolume().add(oc.getVolume()));
            buy.setAvgPrice(calculateAvgPrice(oc.getBuyOrderId()));
            buy.setUpdateTime(new Date());
            if (buy.getEntrustVolume().compareTo(buy.getFilledVolume()) == 0) {
                buy.setState(PoConstant.Trade.State.Done);
            }
            tradeMapper.updateByPrimaryKey(buy);

            Trade sell = tradeMapper.selectByPrimaryKey(oc.getSellOrderId());
            sell.setFilledVolume(sell.getFilledVolume().add(oc.getVolume()));
            sell.setAvgPrice(calculateAvgPrice(oc.getSellOrderId()));
            sell.setUpdateTime(new Date());
            if (sell.getEntrustVolume().compareTo(sell.getFilledVolume()) == 0) {
                sell.setState(PoConstant.Trade.State.Done);
            }
            tradeMapper.updateByPrimaryKey(sell);
        }

        // update account 在链上成交之后

        return tradeChildren;
    }

    @Transactional
    public void updateOrderCancel(String tradeId) {
        Trade trade = tradeMapper.selectByPrimaryKey(tradeId);
        String state = PoConstant.Trade.State.CancelPart;
        if (trade.getFilledVolume().compareTo(BigDecimal.ZERO) == 0) {
            state = PoConstant.Trade.State.Cancel;
        }
        trade.setState(state);
        tradeMapper.updateByPrimaryKey(trade);

        // update unlock sut
        BigDecimal unlockSut = trade.getEntrustVolume().subtract(trade.getFilledVolume()).multiply(trade.getEntrustPrice());
        UserAccount userAccount = userAccountMapper.selectByPrimaryKey(trade.getUserAddress());
        userAccount.setSut(userAccount.getSut().add(unlockSut));
        userAccount.setSutLock(userAccount.getSutLock().subtract(unlockSut));
        userAccountMapper.updateByPrimaryKey(userAccount);
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

    @Transactional
    public String addTakePlan(String tradeId, Integer childSize, Integer times, Long gasPrice, Long gasLimit, Long timestamp, String sign) {
        if (times <= 0) {
            return null;
        }
        String id = idGenerator.getHexStringId();
        TakePlan plan = new TakePlan();
        plan.setTakePlanId(id);
        plan.setTakeTradeId(tradeId);
        plan.setChildSize(childSize);
        plan.setTimes(times);
        plan.setGasPrice(gasPrice);
        plan.setGasLimit(gasLimit);
        plan.setTimestamp(timestamp);
        plan.setSign(sign);
        plan.setIsOver(false);
        plan.setCreateTime(new Date());
        takePlanMapper.insert(plan);
        return id;
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
