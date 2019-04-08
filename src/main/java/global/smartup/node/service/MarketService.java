package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.SmartupClient;
import global.smartup.node.mapper.MarketMapper;
import global.smartup.node.po.Market;
import global.smartup.node.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketService.class);

    @Autowired
    private MarketMapper marketMapper;

    @Autowired
    private SmartupClient smartupClient;

    @Autowired
    private RedisTemplate redisTemplate;

    public void create(Market market) {
        market.setStage(PoConstant.Market.Stage.Creating);
        market.setCreateTime(new Date());
        marketMapper.insert(market);
    }

    public void updateCreatingToBuilt() {
        Example example = new Example(Market.class);
        example.createCriteria().andEqualTo("stage", PoConstant.Market.Stage.Creating);
        example.orderBy("createTime").asc();
        List<Market> list = marketMapper.selectByExample(example);
        for (Market market : list) {
            String ctAddress = smartupClient.getCtMarketAddress(market.getTxHash());
            if (ctAddress != null) {
                market.setMarketAddress(ctAddress);
                market.setStage(PoConstant.Market.Stage.Built);
                marketMapper.updateByPrimaryKey(market);
            }
        }
    }

    public void updateBuilt(String txHash, String address) {
        Market cdt = new Market();
        cdt.setTxHash(txHash);
        cdt.setMarketAddress(address);
        List<Market> list = marketMapper.select(cdt);
        if (list.size() != 1) {
            log.error("Can not insert market repeat. txHash={}, address={}", txHash, address);
        }
        Market m = list.get(0);
        m.setCreatorAddress(address);
        m.setStage(PoConstant.Market.Stage.Built);
        marketMapper.updateByPrimaryKey(m);
    }

    public boolean isTxHashExist(String txHash) {
        return marketMapper.selectByPrimaryKey(txHash) != null ? true : false;
    }

    public Market queryByTxHash(String txHash) {
        return marketMapper.selectByPrimaryKey(txHash);
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
        Page page = PageHelper.startPage(pageNumb, pageSize);
        marketMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

}
