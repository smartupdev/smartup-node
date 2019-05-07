package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.TransactionMapper;
import global.smartup.node.po.Transaction;
import global.smartup.node.util.Common;
import global.smartup.node.util.Pagination;
import global.smartup.node.vo.Tx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private MarketService marketService;

    public void addPending(String txHash, String type) {
        Transaction tx = transactionMapper.selectByPrimaryKey(txHash);
        if (tx == null) {
            tx = new Transaction();
            tx.setTxHash(txHash);
            tx.setStage(PoConstant.TxStage.Pending);
            tx.setType(type);
            tx.setCreateTime(new Date());
            transactionMapper.insert(tx);
        }
    }

    public void addCreateMarket(String txHash, String marketId, String userAddress) {
        Transaction ts = query(txHash);
        if (ts == null) {
            add(txHash, PoConstant.TxStage.Pending, PoConstant.Transaction.Type.CreateMarket, userAddress, marketId,
                    null, null, new Date(), null);
        } else {
            log.error("Add create market transaction error, repeat txHash = {}" , txHash);
        }
    }

    public void modCreateMarketFinish(String txHash, String stage, String userAddress, String marketId,
                                      String marketAddress, BigDecimal sutAmount, Date blockTime) {
        Transaction ts = query(txHash);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sut", sutAmount);
        if (ts == null) {
            add(txHash, stage, PoConstant.Transaction.Type.CreateMarket, userAddress, marketId, marketAddress, map,
                    new Date(), blockTime);
        } else {
            ts.setStage(stage);
            ts.setUserAddress(userAddress);
            ts.setMarketId(marketId);
            ts.setMarketAddress(marketAddress);
            ts.setDetail(JSON.toJSONString(map, SerializerFeature.WriteBigDecimalAsPlain));
            ts.setBlockTime(blockTime);
            transactionMapper.updateByPrimaryKey(ts);
        }
    }

    public void addTrade(String txHash, String type, String userAddress, String marketId, String marketAddress,
                         BigDecimal sut, BigDecimal ct) {
        Transaction ts = query(txHash);
        if (ts == null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("sut", sut);
            map.put("ct", ct);
            add(txHash, PoConstant.TxStage.Pending, type, userAddress,  marketId, marketAddress, map, new Date(),
                    null);
        } else {
            log.error("Add trade transaction error, repeat txHash = {}" , txHash);
        }
    }

    public void modTradeFinish(String txHash, String stage, String type, String userAddress, String marketId,
                               String marketAddress, BigDecimal sut, BigDecimal ct, Date blockTime) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("sut", sut);
        map.put("ct", ct);
        Transaction ts = query(txHash);
        if (ts == null) {
            add(txHash, stage, type, userAddress, marketId, marketAddress, map, new Date(), blockTime);
        } else {
            ts.setStage(stage);
            ts.setUserAddress(userAddress);
            ts.setMarketId(marketId);
            ts.setMarketAddress(marketAddress);
            ts.setDetail(JSON.toJSONString(map, SerializerFeature.WriteBigDecimalAsPlain));
            ts.setBlockTime(blockTime);
            transactionMapper.updateByPrimaryKey(ts);
        }
    }

    private void add(String txHash, String stage, String type, String userAddress, String marketId, String marketAddress,
                    HashMap<String, Object> detail, Date createTime, Date blockTime) {
        Transaction ts = new Transaction();
        ts.setTxHash(txHash);
        ts.setStage(stage);
        ts.setType(type);
        ts.setUserAddress(userAddress);
        ts.setMarketAddress(marketAddress);
        ts.setMarketId(marketId);
        if (detail != null) {
            ts.setDetail(JSON.toJSONString(detail, SerializerFeature.WriteBigDecimalAsPlain));
        }
        ts.setCreateTime(createTime);
        ts.setBlockTime(blockTime);
        transactionMapper.insert(ts);
    }

    public boolean isLastTradeTransactionInSegment(Date time, String segment) {
        Date end = Common.getEndTimeInSegment(segment, time);
        Example example = new Example(Transaction.class);
        example.createCriteria().andGreaterThan("blockTime", time).andLessThanOrEqualTo("blockTime", end);
        int count = transactionMapper.selectCountByExample(example);
        return count == 0;
    }

    public List<Transaction> queryPendingList() {
        Example example = new Example(Transaction.class);
        example.createCriteria()
                .andEqualTo("stage", PoConstant.TxStage.Pending)
                .andGreaterThan("createTime", Common.getSomeHoursAgo(new Date(), 24));
        return transactionMapper.selectByExample(example);
    }

    public Transaction query(String txHash) {
        return transactionMapper.selectByPrimaryKey(txHash);
    }

    public Pagination<Tx> queryPage(String userAddress, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Transaction.class);
        example.createCriteria()
                .andEqualTo("userAddress", userAddress);
        example.orderBy("createTime").desc();
        Page<Transaction> page = PageHelper.startPage(pageNumb, pageSize);
        transactionMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageSize(), page.getPageNum(), transferVo(page.getResult()));
    }

    private List<Tx> transferVo(List<Transaction> list) {
        List<Tx> ret = new ArrayList<>();
        List<String> marketIds = new ArrayList<>();
        list.forEach(ts -> {
            Tx tx = new Tx();
            BeanUtils.copyProperties(ts, tx, "detail");
            if (StringUtils.isNotBlank(ts.getDetail())) {
                HashMap map = JSON.parseObject(ts.getDetail(), HashMap.class, Feature.UseBigDecimal);
                tx.setDetail(map);
                ret.add(tx);
            }
            marketIds.add(ts.getMarketId());
        });
        List<String> marketNames = marketService.queryNames(marketIds);
        for (int i = 0; i < ret.size(); i++) {
            ret.get(i).setMarketName(marketNames.get(i));
        }
        return ret;
    }
}
