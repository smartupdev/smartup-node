package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.info.BuyCTInfo;
import global.smartup.node.eth.info.CreateMarketInfo;
import global.smartup.node.eth.info.SellCTInfo;
import global.smartup.node.mapper.MarketDataMapper;
import global.smartup.node.mapper.MarketMapper;
import global.smartup.node.po.Market;
import global.smartup.node.po.MarketData;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketService.class);

    private static List<String> CacheMarketAddresses = null;

    @Autowired
    private MarketMapper marketMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private MarketDataMapper marketDataMapper;

    @Autowired
    private KlineNodeService klineNodeService;


    public void save(Market market) {
        Market current = queryCurrentCreating(market.getCreatorAddress());
        if (current != null) {
            current.setName(market.getName());
            current.setDescription(market.getDescription());
            marketMapper.updateByPrimaryKey(current);
        } else {
            market.setMarketId(idGenerator.getStringId());
            market.setStage(PoConstant.Market.Stage.Creating);
            market.setCreateTime(new Date());
            marketMapper.insert(market);
        }
    }

    /**
     * 创建市场事件，更新市场
     */
    public void updateCreateByChain(CreateMarketInfo info) {
        if (info == null) {
            return;
        }
        if (isTxHashExist(info.getTxHash())) {
            return;
        }
        Market market = queryCurrentCreating(info.getEventCreatorAddress());
        if (market == null) {
            log.error("Can not find create market info by user = {}, hash = {}", info.getEventMarketAddress(), info.getTxHash());
            return;
        }
        market.setTxHash(info.getTxHash());
        market.setMarketAddress(info.getEventMarketAddress());
        market.setStage(PoConstant.Market.Stage.Built);
        marketMapper.updateByPrimaryKey(market);
        log.info("Find market on chain, market = {}", JSON.toJSONString(market));

        // create market data
        MarketData data = new MarketData();
        data.setMarketAddress(info.getEventMarketAddress());
        data.setLatelyChange(null);
        data.setLatelyVolume(BigDecimal.ZERO);
        data.setAmount(BigDecimal.ZERO);
        data.setCtAmount(BigDecimal.ZERO);
        data.setCtTopAmount(BigDecimal.ZERO);
        data.setCount(0L);
        marketDataMapper.insert(data);

        // clear cache
        CacheMarketAddresses = null;
    }

    /**
     * 购买CT事件，更新市场数据
     */
    public void updateBuyTradeByChain(BuyCTInfo info) {
        String marketAddress = info.getEventMarketAddress();
        MarketData data = marketDataMapper.selectByPrimaryKey(marketAddress);
        if (data == null) {
            log.error("Can not find market data, market address = {}", marketAddress);
            return;
        }
        BigDecimal price = info.getEventSUT().divide(info.getEventCT(), 20, RoundingMode.DOWN);
        data.setLatelyChange(klineNodeService.queryLatelyChange(marketAddress, price, 24));
        data.setLatelyVolume(klineNodeService.queryLatelyVolume(info.getEventMarketAddress(), 24));
        data.setLast(price);
        data.setAmount(data.getAmount().add(info.getEventSUT()));
        data.setCtAmount(data.getCtAmount().add(info.getEventCT()));
        // ctAmount 变大，重新计算ctTopAmount
        if (data.getCtAmount().compareTo(data.getCtTopAmount()) > 0) {
            data.setCtTopAmount(data.getCtAmount());
        }
        data.setCount(data.getCount() + 1);
        marketDataMapper.updateByPrimaryKey(data);
    }

    /**
     * 出售CT事件，更新市场数据
     */
    public void updateSellTradeByChain(SellCTInfo info) {
        String marketAddress = info.getEventMarketAddress();
        MarketData data = marketDataMapper.selectByPrimaryKey(marketAddress);
        if (data == null) {
            log.error("Can not find market data, market address = {}", marketAddress);
            return;
        }
        BigDecimal price = info.getEventSUT().divide(info.getEventCT(), 20, RoundingMode.DOWN);
        data.setLatelyChange(klineNodeService.queryLatelyChange(marketAddress, price, 24));
        data.setLatelyVolume(klineNodeService.queryLatelyVolume(info.getEventMarketAddress(), 24));
        data.setLast(price);
        data.setAmount(data.getAmount().subtract(info.getEventSUT()));
        data.setCtAmount(data.getCtAmount().subtract(info.getEventCT()));
        data.setCount(data.getCount() + 1);
        marketDataMapper.updateByPrimaryKey(data);
    }

    public boolean isNameRepeat(String userAddress, String name) {
        Market cdt = new Market();
        cdt.setName(name);
        List<Market> list = marketMapper.select(cdt);
        Iterator<Market> iterator = list.iterator();
        while (iterator.hasNext()) {
            Market m = iterator.next();
            if (m.getCreatorAddress().equals(userAddress) && m.getStage().equals(PoConstant.Market.Stage.Creating)) {
                iterator.remove();
            }
        }
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isMarketAddressInCache(String marketAddress) {
        if (CacheMarketAddresses == null) {
            CacheMarketAddresses = new ArrayList<>();
            queryAll().forEach(m -> CacheMarketAddresses.add(m.getMarketAddress()));
        }
        return CacheMarketAddresses.contains(marketAddress);
    }

    public boolean isMarketIdExist(String marketId) {
        return marketMapper.selectByPrimaryKey(marketId) != null;
    }

    public boolean isTxHashExist(String txHash) {
        return queryByTxHash(txHash) != null;
    }

    public Market queryCurrentCreating(String userAddress) {
        Market cdt = new Market();
        cdt.setCreatorAddress(userAddress);
        cdt.setStage(PoConstant.Market.Stage.Creating);
        return marketMapper.selectOne(cdt);
    }

    public Market queryById(String id) {
        return marketMapper.selectByPrimaryKey(id);
    }

    public Market queryByTxHash(String txHash) {
        Market cdt = new Market();
        cdt.setTxHash(txHash);
        return marketMapper.selectOne(cdt);
    }

    public Market queryByAddress(String address) {
        Market cdt = new Market();
        cdt.setMarketAddress(address);
        return marketMapper.selectOne(cdt);
    }

    public List<Market> queryAll() {
        return marketMapper.selectAll();
    }

    public Pagination<Market> queryByCreator(String address, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Market.class);
        example.createCriteria().andEqualTo("creatorAddress", address);
        example.orderBy("createTime").desc();
        Page<Market> page = PageHelper.startPage(pageNumb, pageSize);
        marketMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public List<String> queryBuiltAndHasTrade() {
        List<String> ret = new ArrayList<>();
        Example example = new Example(Market.class);
        example.createCriteria().andEqualTo("stage", PoConstant.Market.Stage.Built);
        example.orderBy("createTime").asc();
        example.excludeProperties("txHash", "creatorAddress", "name", "description", "type", "stage", "createTime");
        List<Market> markets = marketMapper.selectByExample(example);
        markets.forEach(m -> ret.add(m.getMarketAddress()));
        return ret;
    }

    public Pagination<Market> queryPage(String orderBy, Boolean asc, Integer pageNumb, Integer pageSize) {
        List<String> orders = Arrays.asList("lately_change", "last", "lately_volume", "amount", "count");
        if (StringUtils.isBlank(orderBy)|| !orders.contains(orderBy)) {
            orderBy = "amount";
        }
        if (asc == null) {
            asc = false;
        }
        Page<Market> page = PageHelper.startPage(pageNumb, pageSize);
        marketMapper.selectOrderBy(orderBy, asc);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Integer queryMarketCount() {
        Example example = new Example(Market.class);
        example.createCriteria().andIn("stage", Arrays.asList("built"));
        return marketMapper.selectCountByExample(example);
    }

    public BigDecimal queryAllMarketSUTAmount() {
        return marketDataMapper.selectAllMarketAmount(Arrays.asList("built"));
    }

}

