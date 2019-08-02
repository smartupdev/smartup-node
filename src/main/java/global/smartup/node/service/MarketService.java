package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.Config;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.constant.RedisKey;
import global.smartup.node.eth.info.CTBuyInfo;
import global.smartup.node.eth.info.CTSellInfo;
import global.smartup.node.mapper.MarketDataMapper;
import global.smartup.node.mapper.MarketMapper;
import global.smartup.node.po.Collect;
import global.smartup.node.po.Market;
import global.smartup.node.po.MarketData;
import global.smartup.node.po.User;
import global.smartup.node.service.block.BlockMarketService;
import global.smartup.node.util.Common;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.utils.Convert;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MarketService extends BaseService {

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
    private RedisTemplate redisTemplate;

    @Autowired
    private KlineNodeService klineNodeService;

    @Autowired
    private CollectService collectService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CTAccountService ctAccountService;

    @Autowired
    private BlockMarketService blockMarketService;


    public Map<String, String> checkMarketInfo(String userAddress, String name, String description, String detail, String photo, String cover) {
        Map<String, String> err = new HashMap<>();
        if (name == null || 3 > name.length() || name.length() > 40) {
            err.put("name", getLocaleMsg(LangHandle.MarketNameLengthError));
        } else {
            if (StringUtils.isBlank(userAddress)) {
                if (isNameRepeat(name)) {
                    err.put("name", getLocaleMsg(LangHandle.MarketNameRepeat));
                }
            } else {
                if (isNameRepeat(userAddress, name)) {
                    err.put("name", getLocaleMsg(LangHandle.MarketNameRepeat));
                }
            }
        }
        if (!isDescriptionLenRight(description)) {
            err.put("description", getLocaleMsg(LangHandle.MarketDescriptionLengthError));
        }
        if (StringUtils.isNotBlank(detail)) {
            if (detail.getBytes().length >  16_777_215) {
                err.put("detail", getLocaleMsg(LangHandle.MarketDetailMaxLengthError));
            }
        }
        if (StringUtils.isBlank(photo)) {
            err.put("photo", getLocaleMsg(LangHandle.MarketPhotoNotNull));

        }
        if (StringUtils.isBlank(cover)) {
            err.put("cover", getLocaleMsg(LangHandle.MarketCoverNotNull));
        }
        return err;
    }

    public Map<String, String> checkMarketSetting(String userAddress, String symbol, BigDecimal ctCount, BigDecimal ctPrice, BigDecimal ctRecyclePrice, Long closingTime) {
        Map<String, String> err = new HashMap<>();
        if (symbol == null || 3 > symbol.length() || symbol.length() > 6 || !Common.isLetterDigit(symbol)) {
            err.put("symbol", getLocaleMsg(LangHandle.MarketSymbolLengthError));
        } else {
            if (StringUtils.isBlank(userAddress)) {
                if (isSymbolRepet(symbol)) {
                    err.put("symbol", getLocaleMsg(LangHandle.MarketSymbolRepeatError));
                }
            } else {
                if (isSymbolRepet(userAddress, symbol)) {
                    err.put("symbol", getLocaleMsg(LangHandle.MarketSymbolRepeatError));
                }
            }
        }
        if (ctCount == null) {
            err.put("ctCount", getLocaleMsg(LangHandle.MarketCtCountNotNull));
        } else {
            if (ctCount.compareTo(BigDecimal.ZERO) < 0) {
                err.put("ctCount", getLocaleMsg(LangHandle.MarketCtCountOverZero));
            }
        }
        if (ctPrice == null) {
            err.put("ctPrice", getLocaleMsg(LangHandle.MarketCtPriceNotNull));
        } else {
            if (ctPrice.compareTo(BigDecimal.ZERO) < 0) {
                err.put("ctPrice", getLocaleMsg(LangHandle.MarketCtPriceOverZero));
            }
        }
        if (ctRecyclePrice == null) {
            err.put("ctRecyclePrice", getLocaleMsg(LangHandle.MarketPercentOfCtPriceNotNull));
        } else {
            if (ctPrice != null) {
                if (ctRecyclePrice.compareTo(BigDecimal.ZERO) < 0
                        || ctRecyclePrice.compareTo(ctPrice) > 0) {
                    err.put("ctRecyclePrice", getLocaleMsg(LangHandle.MarketPercentOfCtPriceRangeError));
                }
            }
        }
        if (closingTime == null) {
            err.put("closingTime", getLocaleMsg(LangHandle.MarketClosingTimeNullError));
        } else {
            long now = System.currentTimeMillis();
            closingTime = closingTime * 1000;
            if (closingTime < now + 24 * 60 * 60 * 1000L || closingTime > now + 90 * 24 * 60 * 60 * 1000L) {
                err.put("closingTime", getLocaleMsg(LangHandle.MarketClosingTimeRangeError));
            }
        }
        return err;
    }

    public Market saveAndPay(String marketId, String userAddress, String name, String symbol, String description, String photo,
                             String cover, BigDecimal ctCount, BigDecimal ctPrice, BigDecimal ctRecyclePrice, Long closingTime,
                             BigInteger gasLimit, BigInteger gasPrice, String sign) {

        // insert or update market
        Market market = queryById(marketId);
        if (market == null) {
            market = new Market();
            market.setMarketId(marketId);
            market.setName(name);
            market.setSymbol(symbol);
            market.setCreatorAddress(userAddress);
            market.setDescription(description);
            market.setPhoto(photo);
            market.setCover(cover);
            market.setInitSut(BuConstant.MarketInitSut);
            market.setCtCount(ctCount);
            market.setCtPrice(ctPrice);
            market.setCtRecyclePrice(ctRecyclePrice);
            market.setClosingTime(new Date(closingTime * 1000));
            market.setCreateTime(new Date());
            market.setStatus(PoConstant.Market.Status.Creating);
            marketMapper.insert(market);
        } else {
            market.setName(name);
            market.setSymbol(symbol);
            market.setCreatorAddress(userAddress);
            market.setDescription(description);
            market.setPhoto(photo);
            market.setCover(cover);
            market.setInitSut(BuConstant.MarketInitSut);
            market.setCtCount(ctCount);
            market.setCtPrice(ctPrice);
            market.setCtRecyclePrice(ctRecyclePrice);
            market.setClosingTime(new Date(closingTime * 1000));
            market.setCreateTime(new Date());
            market.setStatus(PoConstant.Market.Status.Creating);
            marketMapper.updateByPrimaryKey(market);
        }

        // pay and update market
        String txHash = blockMarketService.createMarket(userAddress, BuConstant.MarketInitSut, marketId, symbol, ctCount,
                ctPrice, ctRecyclePrice, closingTime, gasLimit, gasPrice, sign);
        if (txHash != null) {
            market.setStatus(PoConstant.Market.Status.Locked);
            market.setTxHash(txHash);
            marketMapper.updateByPrimaryKey(market);

            // add pending
            transactionService.addPending(txHash, userAddress, PoConstant.Transaction.Type.CreateMarket);
        }

        return market;
    }

    public void updateLock(String marketId, String txHash) {
        Market market = marketMapper.selectByPrimaryKey(marketId);
        if (market != null && PoConstant.Market.Status.Creating.equals(market.getStatus())) {
            market.setStatus(PoConstant.Market.Status.Locked);
            market.setTxHash(txHash);
            marketMapper.updateByPrimaryKey(market);

            // save ts
            transactionService.addCreateMarket(txHash, marketId, market.getCreatorAddress());
        }
    }

    /**
     * 创建市场完成
     */
    public void updateCreateMarketFinish(String marketId, boolean isSuccess, String marketAddress,
                                         BigDecimal initSut, BigDecimal ctCount, BigDecimal ctPrice, BigDecimal ctRecyclePrice) {
        Market market = queryById(marketId);
        if (market == null) {
            return;
        }
        if (isSuccess) {
            market.setMarketAddress(marketAddress);
            market.setStatus(PoConstant.Market.Status.Open);
            market.setInitSut(initSut);
            market.setCtCount(ctCount);
            market.setCtPrice(ctPrice);
            market.setCtRecyclePrice(ctRecyclePrice);
            marketMapper.updateByPrimaryKey(market);

            MarketData data = new MarketData();
            data.setMarketAddress(marketAddress);
            data.setLast(null);
            data.setLatelyChange(null);
            data.setLatelyVolume(null);
            data.setAmount(null);
            data.setCtAmount(null);
            data.setCtTopAmount(null);
            data.setCount(null);
            data.setPostCount(null);
            data.setUserCount(null);
            marketDataMapper.insert(data);
        } else {
            market.setMarketAddress(marketAddress);
            market.setStatus(PoConstant.Market.Status.Fail);
            market.setInitSut(initSut);
            market.setCtCount(ctCount);
            market.setCtPrice(ctPrice);
            market.setCtRecyclePrice(ctRecyclePrice);
            marketMapper.updateByPrimaryKey(market);
        }

        // clear cache
        CacheMarketAddresses = null;
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
        if (data.getAmount() == null) {
            data.setAmount(info.getEventSUT());
        } else {
            data.setAmount(data.getAmount().add(info.getEventSUT()));
        }
        if (data.getCtAmount() == null) {
            data.setCtAmount(info.getEventCT());
            data.setCtTopAmount(info.getEventCT());
        } else {
            data.setCtAmount(data.getCtAmount().add(info.getEventCT()));
            // ctAmount 变大，重新计算ctTopAmount
            if (data.getCtAmount().compareTo(data.getCtTopAmount()) > 0) {
                data.setCtTopAmount(data.getCtAmount());
            }
        }
        if (data.getCount() == null) {
            data.setCount(1L);
        } else {
            data.setCount(data.getCount() + 1);
        }
        Integer userAccount = ctAccountService.queryUserCountInMarket(marketAddress);
        data.setUserCount(userAccount);
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
        if (data.getAmount() == null) {
            data.setAmount(info.getEventSUT());
        } else {
            data.setAmount(data.getAmount().subtract(info.getEventSUT()));
        }
        if (data.getCtAmount() == null) {
            data.setCtAmount(info.getEventCT());
        } else {
            data.setCtAmount(data.getCtAmount().subtract(info.getEventCT()));
        }
        if (data.getCount() == null) {
            data.setCount(1L);
        } else {
            data.setCount(data.getCount() + 1);
        }
        Integer userAccount = ctAccountService.queryUserCountInMarket(marketAddress);
        data.setUserCount(userAccount);
        marketDataMapper.updateByPrimaryKey(data);
    }

    public void updateUserCount(String marketAddress, Integer addend) {
        MarketData data = marketDataMapper.selectByPrimaryKey(marketAddress);
        if (data != null) {
            if (data.getUserCount() == null) {
                data.setUserCount(0);
            }
            if (data.getUserCount() + addend <= 0) {
                data.setUserCount(0);
            } else {
                data.setUserCount(data.getUserCount() + addend);
            }
            marketDataMapper.updateByPrimaryKey(data);
        }
    }

    public void updatePostCountAddOne(String marketAddress) {
        MarketData data = marketDataMapper.selectByPrimaryKey(marketAddress);
        if (data != null) {
            if (data.getPostCount() == null) {
                data.setPostCount(0);
            }
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
                market.setStatus(PoConstant.Market.Status.Creating);
                marketMapper.updateByPrimaryKey(market);
                log.info("Market [{}] lock expire, txHash = {}", market.getMarketId(), market.getTxHash());
            }
        }
    }

    public void updateLatelyChange() {
        List<MarketData> dataList = marketDataMapper.selectAll();
        for (MarketData data : dataList) {
            data.setLatelyChange(klineNodeService.queryLatelyChange(data.getMarketAddress(), data.getLast(), 24));
            marketDataMapper.updateByPrimaryKey(data);
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
                &&
                (
                    PoConstant.Market.Status.Creating.equals(m.getStatus())
                        || PoConstant.Market.Status.Fail.equals(m.getStatus())
                )
            ) {
                iterator.remove();
            }
        }
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isNameRepeat(String name) {
        Market cdt = new Market();
        cdt.setName(name);
        List<Market> list = marketMapper.select(cdt);
        return list.size() > 0;
    }

    public boolean isSymbolRepet(String symbol) {
        Market cdt = new Market();
        cdt.setSymbol(symbol);
        List<Market> list = marketMapper.select(cdt);
        return list.size() > 0;
    }

    public boolean isSymbolRepet(String userAddress, String symbol) {
        Market cdt = new Market();
        cdt.setSymbol(symbol);
        List<Market> list = marketMapper.select(cdt);
        Iterator<Market> iterator = list.iterator();
        while (iterator.hasNext()) {
            Market m = iterator.next();
            if (m.getCreatorAddress().equals(userAddress)
                &&
                (
                    PoConstant.Market.Status.Creating.equals(m.getStatus())
                        || PoConstant.Market.Status.Fail.equals(m.getStatus())
                )
            ) {
                iterator.remove();
            }
        }
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isIdRepeat(String id) {
        return marketMapper.selectByPrimaryKey(id) != null;
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
                .andIn("status",
                        Arrays.asList(PoConstant.Market.Status.Creating, PoConstant.Market.Status.Locked, PoConstant.Market.Status.Fail)
                );
        List<Market> list = marketMapper.selectByExample(example);
        if (list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            log.error("Redundant creating market, User address = {}");
        }
        return list.get(0);
    }

    public Market queryById(String id) {
        return marketMapper.selectByPrimaryKey(id);
    }

    public Market queryWithDataById(String id) {
        Market market = marketMapper.selectByPrimaryKey(id);
        if (market != null) {
            market.setData(marketDataMapper.selectByPrimaryKey(market.getMarketAddress()));
            market.setCreator(userService.query(market.getCreatorAddress()));
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

    public String queryIdByAddress(String marketAddress) {
        Market cdt = new Market();
        cdt.setMarketAddress(marketAddress);
        Market market = marketMapper.selectOne(cdt);
        if (market != null) {
            return market.getMarketId();
        } else {
            return null;
        }
    }

    public List<Market> queryAll() {
        return marketMapper.selectAll();
    }

    public Pagination<Market> queryByCreator(String address, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Market.class);
        example.createCriteria()
                .andEqualTo("creatorAddress", address)
                .andIn("status", Arrays.asList(PoConstant.Market.Status.Open, PoConstant.Market.Status.Close));
        example.orderBy("createTime").desc();
        Page<Market> page = PageHelper.startPage(pageNumb, pageSize);
        marketMapper.selectByExample(example);

        fillMarketData(page.getResult());
        queryUserCollect(address, page.getResult());
        querySevenDayNode(page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public List<String> queryBuiltAndHasTrade() {
        List<String> ret = new ArrayList<>();
        Example example = new Example(Market.class);
        example.createCriteria().andEqualTo("stage", PoConstant.TxStage.Success);
        example.orderBy("createTime").asc();
        List<Market> markets = marketMapper.selectByExample(example);
        markets.forEach(m -> ret.add(m.getMarketAddress()));
        return ret;
    }

    public List<String> queryNames(List<String> marketIds) {
        if (marketIds == null || marketIds.size() <= 0) {
            return new ArrayList<>();
        }
        List<String> ret = new ArrayList<>();
        Example example = new Example(Market.class);
        example.createCriteria().andIn("marketId", marketIds);
        example.selectProperties("marketId", "name");
        List<Market> markets = marketMapper.selectByExample(example);
        marketIds.forEach(id -> {
            Optional<Market> o = markets.stream().filter(m -> m.getMarketId().equals(id)).findFirst();
            if (o.isPresent()) {
                ret.add(o.get().getName());
            } else {
                ret.add(null);
            }
        });
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
        } else {
            name = name.replace("_", "\\_");
            name = name.replace("%", "\\%");
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
            market.setIsCollected(is);
        }
    }

    public Pagination<Market> queryUserTraded(String userAddress, Integer pageNumb, Integer pageSize) {
        Pagination<String> page = ctAccountService.queryUserTradeMarket(userAddress, pageNumb, pageSize);
        if (page.getRowCount() <= 0) {
            return Pagination.blank();
        }

        Example example = new Example(Market.class);
        example.createCriteria().andIn("marketAddress", page.getList());
        List<Market> markets = marketMapper.selectByExample(example);

        List<Market> ret = new ArrayList<>();
        for (String add : page.getList()) {
            ret.add(markets.stream().filter(m -> add.equals(m.getMarketAddress())).findFirst().orElse(null));
        }

        fillMarketData(ret);
        queryUserCollect(userAddress, ret);
        querySevenDayNode(ret);

        return Pagination.init(page.getRowCount(), page.getPageNumb(), page.getPageSize(), ret);
    }

    public Pagination<Market> queryUserCollected(String userAddress, Integer pageNumb, Integer pageSize) {
        Pagination<Collect> page = collectService.queryPage(userAddress, PoConstant.Collect.Type.Market, pageNumb, pageSize);
        if (page.getRowCount() <= 0) {
            return Pagination.blank();
        }
        List<String> marketIds = page.getList().stream().map(Collect::getObjectMark).collect(Collectors.toList());


        Example example = new Example(Market.class);
        example.createCriteria().andIn("marketId", marketIds);
        List<Market> markets = marketMapper.selectByExample(example);
        List<Market> ret = new ArrayList<>();
        marketIds.forEach(mId -> markets.stream().filter(m -> mId.equals(m.getMarketId())).findFirst().ifPresent(k -> ret.add(k)));

        fillMarketData(ret);
        queryUserCollect(userAddress, ret);
        querySevenDayNode(ret);

        return Pagination.init(page.getRowCount(), page.getPageNumb(), page.getPageSize(), ret);
    }

    public List<User> queryTopCTUser(String marketId) {
        List<User> ret = new ArrayList<>();
        Market market = marketMapper.selectByPrimaryKey(marketId);
        if (market == null) {
            return ret;
        }
        String key = RedisKey.MarketTopUser.Prefix + market.getMarketAddress() + RedisKey.MarketTopUser.CTTopPrefix;
        Object obj = redisTemplate.opsForValue().get(key);

        if (obj == null) {
            List<String> topAddress = ctAccountService.queryTopUserAddress(market.getMarketAddress(), 5);
            if (topAddress == null || topAddress.size() <= 0) {
                redisTemplate.opsForValue().set(key, RedisKey.NoDataFlag, RedisKey.MarketTopUser.Expire);
                return ret;
            }
            List<User> userList = userService.queryUserList(topAddress);
            topAddress.forEach(add -> userList.stream().filter(u -> u.getUserAddress().equals(add)).findFirst().ifPresent(ret::add));
            redisTemplate.opsForValue().set(key, ret, RedisKey.MarketTopUser.Expire, TimeUnit.MILLISECONDS);
            return ret;
        } else {
            if (RedisKey.NoDataFlag.equals(obj.toString())) {
                return ret;
            } else {
                return (List<User>) obj;
            }
        }
    }

    public List<User> queryTopPostUser(String marketId) {
        List<User> ret = new ArrayList<>();
        Market market = marketMapper.selectByPrimaryKey(marketId);
        if (market == null) {
            return ret;
        }
        String key = RedisKey.MarketTopUser.Prefix + market.getMarketAddress() + RedisKey.MarketTopUser.PostTopPrefix;
        Object obj = redisTemplate.opsForValue().get(key);

        if (obj == null) {
            ret = userService.queryTopUserOnPostCount(marketId, 5);
            redisTemplate.opsForValue().set(key, ret, RedisKey.MarketTopUser.Expire, TimeUnit.MILLISECONDS);
            return ret;
        } else {
            if (RedisKey.NoDataFlag.equals(obj.toString())) {
                return ret;
            } else {
                return (List<User>) obj;
            }
        }
    }

    public List<User> queryTopLikedUser(String marketId) {
        List<User> ret = new ArrayList<>();
        Market market = marketMapper.selectByPrimaryKey(marketId);
        if (market == null) {
            return ret;
        }
        String key = RedisKey.MarketTopUser.Prefix + market.getMarketAddress() + RedisKey.MarketTopUser.LikeTopPrefix;
        Object obj = redisTemplate.opsForValue().get(key);

        if (obj == null) {
            ret = userService.queryTopUserOnReceviedLike(marketId, 5);
            redisTemplate.opsForValue().set(key, ret, RedisKey.MarketTopUser.Expire, TimeUnit.MILLISECONDS);
            return ret;
        } else {
            if (RedisKey.NoDataFlag.equals(obj.toString())) {
                return ret;
            } else {
                return (List<User>) obj;
            }
        }
    }

    public boolean isDescriptionLenRight(String des) {
        if (StringUtils.isBlank(des)) {
            return false;
        } else {
            int len = Common.getLenOfChinese(des) * 2;
            len += Common.getLenOfNotChinese(des);
            if (len > 1 && len <= 2000) {
                return true;
            } else {
                return false;
            }
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
                    m.setIsCollected(true);
                } else {
                    m.setIsCollected(false);
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

    private void fillMarketData(List<Market> markets) {
        if (markets == null || markets.size() <= 0) {
            return;
        }
        List<String> addressList = markets.stream().map(Market::getMarketAddress).collect(Collectors.toList());
        Example example = new Example(MarketData.class);
        example.createCriteria().andIn("marketAddress", addressList);
        List<MarketData> dataList = marketDataMapper.selectByExample(example);
        markets.forEach(m -> m.setData(dataList.stream().filter(d -> d.getMarketAddress().equals(m.getMarketAddress())).findFirst().orElse(null)));
    }

}

