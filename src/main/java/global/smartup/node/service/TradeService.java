package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.ExchangeClient;
import global.smartup.node.mapper.TradeChildMapper;
import global.smartup.node.mapper.TradeMapper;
import global.smartup.node.po.Trade;
import global.smartup.node.po.TradeChild;
import global.smartup.node.po.Transaction;
import global.smartup.node.util.Pagination;
import global.smartup.node.vo.Tx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.utils.Convert;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private TradeChildMapper tradeChildMapper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ExchangeClient exchangeClient;

    @Autowired
    private IdGenerator idGenerator;


    public Trade firstStageBuy(String userAddress, String marketId, String marketAddress,
                                BigDecimal ctCount, BigDecimal ctPrice,
                                BigInteger gasLimit, BigInteger gasPrice, String timestamp, String sign) {
        String txHash = null;
        try {
            txHash = exchangeClient.firstStageBuy(userAddress, marketAddress, ctCount, gasLimit, gasPrice, timestamp, sign);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (txHash == null) {
            return null;
        }
        BigDecimal gasFee = Convert.fromWei(new BigDecimal(gasPrice.multiply(gasLimit)), Convert.Unit.GWEI);
        Date current = new Date();
        String id = idGenerator.getHexStringId();
        Trade trade = new Trade();
        trade.setTradeId(id).setUserAddress(userAddress).setMarketId(marketId)
            .setType(PoConstant.Trade.Type.FirstStageBuy)
            .setEntrustVolume(ctCount).setEntrustPrice(ctPrice)
            .setTradeVolume(BigDecimal.ZERO).setTradePrice(BigDecimal.ZERO)
            .setState(PoConstant.Trade.State.Trading).setFee(gasFee).setCreateTime(current).setUpdateTime(current);
        tradeMapper.insert(trade);

        Transaction tr = transactionService.addPending(txHash, userAddress, PoConstant.Transaction.Type.FirstStageBuyCT);

        TradeChild child = new TradeChild();
        child.setTradeId(id).setTxHash(txHash).setVolume(ctCount).setPrice(ctPrice).setCreateTime(current).setTx(transactionService.transferVo(tr));
        tradeChildMapper.insert(child);
        trade.setChildList(Arrays.asList(child));

        return trade;
    }

    public void modFirstBuyFinish(String txHash, boolean isSuccess) {
        TradeChild child = tradeChildMapper.selectByPrimaryKey(txHash);
        if (child == null) {
            return;
        }
        Trade trade = tradeMapper.selectByPrimaryKey(child.getTradeId());
        if (isSuccess) {
            trade.setTradeVolume(trade.getEntrustVolume());
            trade.setTradePrice(trade.getEntrustPrice());
        }
        trade.setState(PoConstant.Trade.State.Done);
        trade.setUpdateTime(new Date());
        tradeMapper.updateByPrimaryKey(trade);
    }

    public Trade queryById(String tradeId) {
        Trade trade = tradeMapper.selectByPrimaryKey(tradeId);
        if (trade != null) {
            fillChild(Arrays.asList(trade));
            fillTransaction(Arrays.asList(trade));
        }
        return trade;
    }

    public Pagination<Trade> queryByUserTrade(String userAddress, List<String> types, List<String> states, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Trade.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userAddress", userAddress);
        if (types != null && types.size() > 0) {
            criteria.andIn("type", types);
        }
        if (states != null && states.size() > 0) {
            criteria.andIn("state", states);
        }
        example.orderBy("createTime").desc();

        Page<Trade> page = PageHelper.startPage(pageNumb, pageSize);
        tradeMapper.selectByExample(example);
        fillChild(page.getResult());
        fillTransaction(page.getResult());
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    private void fillChild(List<Trade> trades) {
        if (trades == null || trades.size() == 0) {
            return;
        }
        List<String> ids = trades.stream().map(t -> t.getTradeId()).collect(Collectors.toList());
        Example example = new Example(TradeChild.class);
        example.createCriteria().andIn("tradeId", ids);
        example.orderBy("createTime").asc();
        List<TradeChild> children = tradeChildMapper.selectByExample(example);
        for (Trade trade : trades) {
            List<TradeChild> tc = new ArrayList<>();
            for (TradeChild child : children) {
                if (child.getTradeId().equals(trade.getTradeId())) {
                    tc.add(child);
                }
            }
            trade.setChildList(tc);
        }
    }

    private void fillTransaction(List<Trade> trades) {
        if (trades == null || trades.size() == 0) {
            return;
        }
        List<String> txHashList = new ArrayList<>();
        for (Trade trade : trades) {
            txHashList.addAll(trade.getChildList().stream().map(c -> c.getTxHash()).collect(Collectors.toList()));
        }
        List<Tx> transactionList = transactionService.queryTxList(txHashList);
        for (Trade trade : trades) {
            for (TradeChild child : trade.getChildList()) {
                child.setTx(transactionList.stream().filter(t -> t.getTxHash().equals(child.getTxHash())).findFirst().orElse(null));
            }
        }
    }

    // public Trade savePendingTrade(String userAddress, String txHash, String type, String marketId, BigDecimal sut, BigDecimal ct) {
    //     Market market = marketMapper.selectByPrimaryKey(marketId);
    //     Trade trade = new Trade();
    //     trade.setTxHash(txHash);
    //     trade.setMarketAddress(market.getMarketAddress());
    //     trade.setUserAddress(userAddress);
    //     trade.setType(type);
    //     trade.setSutOffer(sut);
    //     trade.setSutAmount(sut);
    //     trade.setCtAmount(ct);
    //     trade.setCreateTime(new Date());
    //     trade.setStage(PoConstant.TxStage.Pending);
    //     tradeMapper.insert(trade);
    //
    //     // save transaction
    //     String transType;
    //     if (PoConstant.Trade.Type.Buy.equals(type)) {
    //         transType = PoConstant.Transaction.Type.BuyCT;
    //     } else {
    //         transType = PoConstant.Transaction.Type.SellCT;
    //     }
    //     transactionService.addTrade(txHash, transType, userAddress, marketId, market.getMarketAddress(), sut, ct);
    //
    //     return trade;
    // }
    //
    // public void saveBuyTxByChain(CTBuyInfo info) {
    //     Trade trade = queryByTxHash(info.getTxHash());
    //     if (trade == null) {
    //         trade = new Trade();
    //         trade.setTxHash(info.getTxHash());
    //         trade.setMarketAddress(info.getEventMarketAddress());
    //         trade.setUserAddress(info.getEventUserAddress());
    //         trade.setStage(PoConstant.TxStage.Success);
    //         trade.setCreateTime(new Date());
    //         trade.setBlockTime(info.getBlockTime());
    //         trade.setType(PoConstant.Trade.Type.Buy);
    //         trade.setSutOffer(info.getEventSUTOffer());
    //         trade.setSutAmount(info.getEventSUT());
    //         trade.setCtAmount(info.getEventCT());
    //         tradeMapper.insert(trade);
    //     } else {
    //         trade.setMarketAddress(info.getEventMarketAddress());
    //         trade.setUserAddress(info.getEventUserAddress());
    //         trade.setStage(PoConstant.TxStage.Success);
    //         trade.setBlockTime(info.getBlockTime());
    //         trade.setType(PoConstant.Trade.Type.Buy);
    //         trade.setSutOffer(info.getEventSUTOffer());
    //         trade.setSutAmount(info.getEventSUT());
    //         trade.setCtAmount(info.getEventCT());
    //         tradeMapper.updateByPrimaryKey(trade);
    //     }
    //
    // }
    //
    // public void saveSellTxByChain(CTSellInfo info) {
    //     Trade trade = queryByTxHash(info.getTxHash());
    //     if (trade == null) {
    //         trade = new Trade();
    //         trade.setTxHash(info.getTxHash());
    //         trade.setMarketAddress(info.getEventMarketAddress());
    //         trade.setUserAddress(info.getEventUserAddress());
    //         trade.setStage(PoConstant.TxStage.Success);
    //         trade.setCreateTime(new Date());
    //         trade.setBlockTime(info.getBlockTime());
    //         trade.setType(PoConstant.Trade.Type.Sell);
    //         trade.setSutAmount(info.getEventSUT());
    //         trade.setCtAmount(info.getEventCT());
    //         tradeMapper.insert(trade);
    //     } else {
    //         trade.setMarketAddress(info.getEventMarketAddress());
    //         trade.setUserAddress(info.getEventUserAddress());
    //         trade.setStage(PoConstant.TxStage.Success);
    //         trade.setBlockTime(info.getBlockTime());
    //         trade.setType(PoConstant.Trade.Type.Sell);
    //         trade.setSutAmount(info.getEventSUT());
    //         trade.setCtAmount(info.getEventCT());
    //         tradeMapper.updateByPrimaryKey(trade);
    //     }
    //
    // }
    //
    // public void saveFailTradeTxByChain(String txHash, String type, String userAddress, String marketAddress, BigDecimal sut, BigDecimal ct, Date blockTime) {
    //     Trade trade = queryByTxHash(txHash);
    //     if (trade == null) {
    //         trade = new Trade();
    //         trade.setTxHash(txHash);
    //         trade.setMarketAddress(marketAddress);
    //         trade.setUserAddress(userAddress);
    //         trade.setStage(PoConstant.TxStage.Fail);
    //         trade.setCreateTime(new Date());
    //         trade.setBlockTime(blockTime);
    //         trade.setType(type);
    //         trade.setSutAmount(sut);
    //         trade.setCtAmount(ct);
    //         tradeMapper.insert(trade);
    //     } else {
    //         trade.setMarketAddress(marketAddress);
    //         trade.setUserAddress(userAddress);
    //         trade.setStage(PoConstant.TxStage.Fail);
    //         trade.setBlockTime(blockTime);
    //         trade.setType(type);
    //         tradeMapper.updateByPrimaryKey(trade);
    //     }
    // }
    //
    // public boolean isTxHashHandled(String txHash) {
    //     Trade t = tradeMapper.selectByPrimaryKey(txHash);
    //     if (t == null) {
    //         return false;
    //     }
    //     if (t.getStage().equals(PoConstant.TxStage.Pending)) {
    //         return false;
    //     }
    //     return true;
    // }
    //
    // public boolean isTxHashExist(String txHash) {
    //     return tradeMapper.selectByPrimaryKey(txHash) != null;
    // }
    //
    // public Trade queryByTxHash(String txHash) {
    //     return tradeMapper.selectByPrimaryKey(txHash);
    // }
    //
    // public Pagination<Trade> queryByUser(String userAddress, String type, Integer pageNumb, Integer pageSize) {
    //     Example example = new Example(Trade.class);
    //     Example.Criteria criteria = example.createCriteria();
    //     criteria.andEqualTo("userAddress", userAddress);
    //     if (StringUtils.isNotBlank(type)) {
    //         criteria.andEqualTo("type", type);
    //     }
    //     example.orderBy("createTime").desc();
    //     Page<Trade> page = PageHelper.startPage(pageNumb, pageSize);
    //     tradeMapper.selectByExample(example);
    //     return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    // }
    //
    // public Pagination<Trade> queryByMarket(String marketAddress, String type, Boolean asc, Integer pageNumb, Integer pageSize) {
    //     if (asc == null) {
    //         asc = false;
    //     }
    //     Page<Trade> page = PageHelper.startPage(pageNumb, pageSize);
    //     tradeMapper.selectOrderBy(marketAddress, type, asc);
    //     return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    // }

}
