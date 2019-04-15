package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.info.CreateMarketInfo;
import global.smartup.node.mapper.MarketMapper;
import global.smartup.node.po.Market;
import global.smartup.node.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketService.class);

    private static List<String> CacheMarketAddresses = null;

    @Autowired
    private MarketMapper marketMapper;

    @Autowired
    private IdGenerator idGenerator;

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
     * 用链上的信息更新市场
     */
    public void updateByChain(CreateMarketInfo info) {
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

        // clear cache
        CacheMarketAddresses = null;
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

    public boolean isMarketExist(String marketAddress) {
        if (CacheMarketAddresses == null) {
            CacheMarketAddresses = new ArrayList<>();
            queryAll().forEach(m -> CacheMarketAddresses.add(m.getMarketAddress()));
        }
        return CacheMarketAddresses.contains(marketAddress);
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

}

