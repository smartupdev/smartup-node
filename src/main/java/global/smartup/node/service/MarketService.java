package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.Config;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.info.CTBuyInfo;
import global.smartup.node.eth.info.CTSellInfo;
import global.smartup.node.eth.info.MarketCreateInfo;
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
import org.web3j.crypto.Keys;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketService.class);

    /**
     * 缓存已有的CT市场地址，方便快速判断一笔tx是否需要记录
     */
    private static List<String> CacheMarketAddresses = null;

    @Autowired
    private Config config;

    @Autowired
    private MarketMapper marketMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private MarketDataMapper marketDataMapper;

    @Autowired
    private KlineNodeService klineNodeService;

    @Autowired
    private CollectService collectService;


    public Market save(Market market) {
        Market current = queryCurrentCreating(market.getCreatorAddress());
        if (current != null) {
            current.setName(market.getName());
            current.setDescription(market.getDescription());
            marketMapper.updateByPrimaryKey(current);
            return current;
        } else {
            market.setMarketId(idGenerator.getStringId());
            market.setStatus(PoConstant.Market.Status.Creating);
            market.setCreateTime(new Date());
            marketMapper.insert(market);
            return market;
        }
    }

    public void updateLock(String marketId, String txHash) {
        Market market = marketMapper.selectByPrimaryKey(marketId);
        if (market != null && PoConstant.Market.Status.Creating.equals(market.getStatus())) {
            market.setStage(PoConstant.TxStage.Pending);
            market.setStatus(PoConstant.Market.Status.Locked);
            market.setTxHash(txHash);
            marketMapper.updateByPrimaryKey(market);
        }
    }

    /**
     * 创建市场成功，更新市场
     */
    public void updateCreateByChain(MarketCreateInfo info) {
        if (info == null) {
            return;
        }
        Market market = queryCurrentCreating(info.getEventCreatorAddress());
        if (market == null) {
            return;
        }
        market.setTxHash(info.getTxHash());
        market.setMarketAddress(info.getEventMarketAddress());
        market.setStage(PoConstant.TxStage.Success);
        market.setStatus(PoConstant.Market.Status.Open);
        marketMapper.updateByPrimaryKey(market);

        // create market data
        MarketData data = new MarketData();
        data.setMarketAddress(info.getEventMarketAddress());
        data.setLatelyChange(null);
        data.setLatelyVolume(BigDecimal.ZERO);
        data.setAmount(BigDecimal.ZERO);
        data.setCtAmount(BigDecimal.ZERO);
        data.setCtTopAmount(BigDecimal.ZERO);
        data.setCount(0L);
        data.setPostCount(0);
        data.setUserCount(0);
        marketDataMapper.insert(data);

        // clear cache
        CacheMarketAddresses = null;
    }

    /**
     *  创建市场失败
     */
    public void updateCreateFailByChain(String txHash, String userAddress) {
        Market market = queryCurrentCreating(userAddress);
        market.setTxHash(txHash);
        market.setStage(PoConstant.TxStage.Fail);
        market.setStatus(PoConstant.Market.Status.Close);
        marketMapper.updateByPrimaryKey(market);
    }

    /**
     * 购买CT事件，更新市场数据
     */
    public void updateBuyTradeByChain(CTBuyInfo info) {
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
    public void updateSellTradeByChain(CTSellInfo info) {
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

    public void updateUserCount(String marketAddress, Integer addend) {
        MarketData data = marketDataMapper.selectByPrimaryKey(marketAddress);
        if (data != null) {
            if (Math.max(data.getUserCount() + addend, 0) == 0) {
                data.setPostCount(0);
            } else {
                data.setPostCount(data.getUserCount() + addend);
            }
            marketDataMapper.updateByPrimaryKey(data);
        }
    }

    public void updatePostCountAddOne(String marketAddress) {
        MarketData data = marketDataMapper.selectByPrimaryKey(marketAddress);
        if (data != null) {
            data.setPostCount(data.getPostCount() + 1);
            marketDataMapper.updateByPrimaryKey(data);
        }
    }

    public void updateExpireLock() {
        Integer minute = config.appBusinessMarketLockExpire;
        Integer ms = minute * 60 * 1000;
        Date now = new Date();
        Market cdt = new Market();
        cdt.setStatus(PoConstant.Market.Status.Locked);
        List<Market> list = marketMapper.select(cdt);
        for (Market market : list) {
            if (now.getTime() - ms > market.getCreateTime().getTime()) {
                market.setStage(null);
                market.setStatus(PoConstant.Market.Status.Creating);
                marketMapper.updateByPrimaryKey(market);
                log.info("Market [{}] lock expire, txHash = {}", market.getMarketId(), market.getTxHash());
            }
        }
    }

    public boolean isNameRepeat(String userAddress, String name) {
        Market cdt = new Market();
        cdt.setName(name);
        List<Market> list = marketMapper.select(cdt);
        Iterator<Market> iterator = list.iterator();
        while (iterator.hasNext()) {
            Market m = iterator.next();
            if (m.getCreatorAddress().equals(userAddress)
                    && PoConstant.Market.Status.Creating.equals(m.getStatus())) {
                iterator.remove();
            }
        }
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isMarketAddressInCache(String marketAddress) {
        marketAddress = Keys.toChecksumAddress(marketAddress);
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
        Example example = new Example(Market.class);
        example.createCriteria()
                .andEqualTo("creatorAddress", userAddress)
                .andIn("status", Arrays.asList(PoConstant.Market.Status.Creating, PoConstant.Market.Status.Locked));
        List<Market> list = marketMapper.selectByExample(example);
        if (list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            log.error("Redundant creating market, User address = {}");
        }
        return list.get(0);
    }

    public Market queryById(String id) {
        Market market = marketMapper.selectByPrimaryKey(id);
        if (market != null) {
            market.setData(marketDataMapper.selectByPrimaryKey(market.getMarketAddress()));
        }
        return market;
    }

    public Market queryByTxHash(String txHash) {
        Market cdt = new Market();
        cdt.setTxHash(txHash);
        Market market = marketMapper.selectOne(cdt);
        if (market != null) {
            market.setData(marketDataMapper.selectByPrimaryKey(market.getMarketAddress()));
        }
        return market;
    }

    public Market queryByAddress(String address) {
        Market cdt = new Market();
        cdt.setMarketAddress(address);
        Market market = marketMapper.selectOne(cdt);
        if (market != null) {
            market.setData(marketDataMapper.selectByPrimaryKey(market.getMarketAddress()));
        }
        return market;
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
        example.createCriteria().andEqualTo("stage", PoConstant.TxStage.Success);
        example.orderBy("createTime").asc();
        example.excludeProperties("txHash", "creatorAddress", "name", "description", "type", "stage", "createTime");
        List<Market> markets = marketMapper.selectByExample(example);
        markets.forEach(m -> ret.add(m.getMarketAddress()));
        return ret;
    }

    public Pagination<Market> queryPage(String userAddress, String orderBy, Boolean asc, Integer pageNumb, Integer pageSize) {
        List<String> orders = Arrays.asList("lately_change", "last", "lately_volume", "amount", "count");
        if (StringUtils.isBlank(orderBy)|| !orders.contains(orderBy)) {
            orderBy = "amount";
        }
        if (asc == null) {
            asc = false;
        }
        Page<Market> page = PageHelper.startPage(pageNumb, pageSize);
        marketMapper.selectNameLikeAndOrderBy(null, true, orderBy, asc);
        queryUserCollect(userAddress, page.getResult());
        querySevenDayNode(page.getResult());
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Market> querySearchPage(String userAddress, String name, String orderBy, Boolean asc, Integer pageNumb, Integer pageSize) {
        if (StringUtils.isBlank(name)) {
            name = null;
        }
        List<String> orders = Arrays.asList("lately_change", "last", "lately_volume", "amount", "count");
        if (StringUtils.isBlank(orderBy)|| !orders.contains(orderBy)) {
            orderBy = "amount";
        }
        if (asc == null) {
            asc = false;
        }
        Page<Market> page = PageHelper.startPage(pageNumb, pageSize);
        marketMapper.selectNameLikeAndOrderBy(name, true, orderBy, asc);
        queryUserCollect(userAddress, page.getResult());
        querySevenDayNode(page.getResult());
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public List<Market> queryTop(String userAddress, String type, Integer limit) {
        List<Market> ret = new ArrayList<>();
        if (limit == null || limit.compareTo(100) > 0 || limit.compareTo(0) < 0) {
            limit = 20;
        }
        if (PoConstant.Market.TopType.Hottest.equals(type)) {
            Page<Market> page = PageHelper.startPage(1, limit);
            marketMapper.selectNameLikeAndOrderBy(null, true, "post_count", false);
            ret = page.getResult();
        }
        if (PoConstant.Market.TopType.Newest.equals(type)) {
            Page<Market> page = PageHelper.startPage(1, limit);
            marketMapper.selectNameLikeAndOrderBy(null, false, "create_time", false);
            ret = page.getResult();
        }
        if (PoConstant.Market.TopType.Populous.equals(type)) {
            Page<Market> page = PageHelper.startPage(1, limit);
            marketMapper.selectNameLikeAndOrderBy(null, true, "user_count", false);
            ret = page.getResult();
        }
        if (PoConstant.Market.TopType.Richest.equals(type)) {
            Page<Market> page = PageHelper.startPage(1, limit);
            marketMapper.selectNameLikeAndOrderBy(null, true, "amount", false);
            ret = page.getResult();
        }
        queryUserCollect(userAddress, ret);
        querySevenDayNode(ret);
        return ret;
    }

    public Integer queryMarketCount() {
        Example example = new Example(Market.class);
        example.createCriteria().andIn("status", Arrays.asList(PoConstant.Market.Status.Open));
        return marketMapper.selectCountByExample(example);
    }

    public BigDecimal queryAllMarketSUTAmount() {
        return marketDataMapper.selectAllMarketAmount(Arrays.asList(PoConstant.Market.Status.Open));
    }

    public void queryUserCollect(String userAddress, Market market) {
        if (StringUtils.isNotBlank(userAddress) && market != null) {
            boolean is = collectService.isCollected(userAddress, PoConstant.Collect.Type.Market, market.getMarketId());
            market.setIsCollect(is);
        }
    }

    private void queryUserCollect(String userAddress, List<Market> list) {
        if (StringUtils.isBlank(userAddress) || list == null || list.size() <= 0) {
            return;
        }
        if (StringUtils.isNotBlank(userAddress)) {
            List<Object> marketIds = list.stream().map(m -> m.getMarketId()).collect(Collectors.toList());
            List<String> collected = collectService.isCollected(userAddress, PoConstant.Collect.Type.Market, marketIds);
            list.forEach(m -> {
                if (collected.contains(m.getMarketId())) {
                    m.setIsCollect(true);
                } else {
                    m.setIsCollect(false);
                }
            });
        }
    }

    private void querySevenDayNode(List<Market> list) {
        for (Market market : list) {
            List<BigDecimal> ns = klineNodeService.querySevenDayNode(market.getMarketAddress());
            market.setSevenDayNode(ns);
        }
    }

}


