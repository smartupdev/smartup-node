package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.info.CTBuyInfo;
import global.smartup.node.eth.info.CTSellInfo;
import global.smartup.node.mapper.MarketMapper;
import global.smartup.node.mapper.TradeMapper;
import global.smartup.node.po.Market;
import global.smartup.node.po.Trade;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private MarketMapper marketMapper;

    @Autowired
    private TransactionService transactionService;

    public Trade savePendingTrade(String userAddress, String txHash, String type, String marketId, BigDecimal sut, BigDecimal ct) {
        Market market = marketMapper.selectByPrimaryKey(marketId);
        Trade trade = new Trade();
        trade.setTxHash(txHash);
        trade.setMarketAddress(market.getMarketAddress());
        trade.setUserAddress(userAddress);
        trade.setType(type);
        trade.setSutOffer(sut);
        trade.setSutAmount(sut);
        trade.setCtAmount(ct);
        trade.setCreateTime(new Date());
        trade.setStage(PoConstant.TxStage.Pending);
        tradeMapper.insert(trade);

        // save transaction
        String transType;
        if (PoConstant.Trade.Type.Buy.equals(type)) {
            transType = PoConstant.Transaction.Type.BuyCT;
        } else {
            transType = PoConstant.Transaction.Type.SellCT;
        }
        transactionService.addTrade(txHash, transType, userAddress, marketId, market.getMarketAddress(), sut, ct);

        return trade;
    }

    public void saveBuyTxByChain(CTBuyInfo info) {
        Trade trade = queryByTxHash(info.getTxHash());
        if (trade == null) {
            trade = new Trade();
            trade.setTxHash(info.getTxHash());
            trade.setMarketAddress(info.getEventMarketAddress());
            trade.setUserAddress(info.getEventUserAddress());
            trade.setStage(PoConstant.TxStage.Success);
            trade.setCreateTime(new Date());
            trade.setBlockTime(info.getBlockTime());
            trade.setType(PoConstant.Trade.Type.Buy);
            trade.setSutOffer(info.getEventSUTOffer());
            trade.setSutAmount(info.getEventSUT());
            trade.setCtAmount(info.getEventCT());
            tradeMapper.insert(trade);
        } else {
            trade.setMarketAddress(info.getEventMarketAddress());
            trade.setUserAddress(info.getEventUserAddress());
            trade.setStage(PoConstant.TxStage.Success);
            trade.setBlockTime(info.getBlockTime());
            trade.setType(PoConstant.Trade.Type.Buy);
            trade.setSutOffer(info.getEventSUTOffer());
            trade.setSutAmount(info.getEventSUT());
            trade.setCtAmount(info.getEventCT());
            tradeMapper.updateByPrimaryKey(trade);
        }

    }

    public void saveSellTxByChain(CTSellInfo info) {
        Trade trade = queryByTxHash(info.getTxHash());
        if (trade == null) {
            trade = new Trade();
            trade.setTxHash(info.getTxHash());
            trade.setMarketAddress(info.getEventMarketAddress());
            trade.setUserAddress(info.getEventUserAddress());
            trade.setStage(PoConstant.TxStage.Success);
            trade.setCreateTime(new Date());
            trade.setBlockTime(info.getBlockTime());
            trade.setType(PoConstant.Trade.Type.Sell);
            trade.setSutAmount(info.getEventSUT());
            trade.setCtAmount(info.getEventCT());
            tradeMapper.insert(trade);
        } else {
            trade.setMarketAddress(info.getEventMarketAddress());
            trade.setUserAddress(info.getEventUserAddress());
            trade.setStage(PoConstant.TxStage.Success);
            trade.setBlockTime(info.getBlockTime());
            trade.setType(PoConstant.Trade.Type.Sell);
            trade.setSutAmount(info.getEventSUT());
            trade.setCtAmount(info.getEventCT());
            tradeMapper.updateByPrimaryKey(trade);
        }

    }

    public void saveFailTradeTxByChain(String txHash, String type, String userAddress, String marketAddress, BigDecimal sut, BigDecimal ct, Date blockTime) {
        Trade trade = queryByTxHash(txHash);
        if (trade == null) {
            trade = new Trade();
            trade.setTxHash(txHash);
            trade.setMarketAddress(marketAddress);
            trade.setUserAddress(userAddress);
            trade.setStage(PoConstant.TxStage.Fail);
            trade.setCreateTime(new Date());
            trade.setBlockTime(blockTime);
            trade.setType(type);
            trade.setSutAmount(sut);
            trade.setCtAmount(ct);
            tradeMapper.insert(trade);
        } else {
            trade.setMarketAddress(marketAddress);
            trade.setUserAddress(userAddress);
            trade.setStage(PoConstant.TxStage.Fail);
            trade.setBlockTime(blockTime);
            trade.setType(type);
            tradeMapper.updateByPrimaryKey(trade);
        }
    }

    public boolean isTxHashHandled(String txHash) {
        Trade t = tradeMapper.selectByPrimaryKey(txHash);
        if (t == null) {
            return false;
        }
        if (t.getStage().equals(PoConstant.TxStage.Pending)) {
            return false;
        }
        return true;
    }

    public boolean isTxHashExist(String txHash) {
        return tradeMapper.selectByPrimaryKey(txHash) != null;
    }

    public Trade queryByTxHash(String txHash) {
        return tradeMapper.selectByPrimaryKey(txHash);
    }

    public Pagination<Trade> queryByUser(String userAddress, String type, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Trade.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userAddress", userAddress);
        if (StringUtils.isNotBlank(type)) {
            criteria.andEqualTo("type", type);
        }
        example.orderBy("createTime").desc();
        Page<Trade> page = PageHelper.startPage(pageNumb, pageSize);
        tradeMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Trade> queryByMarket(String marketAddress, String type, Boolean asc, Integer pageNumb, Integer pageSize) {
        if (asc == null) {
            asc = false;
        }
        Page<Trade> page = PageHelper.startPage(pageNumb, pageSize);
        tradeMapper.selectOrderBy(marketAddress, type, asc);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

}
