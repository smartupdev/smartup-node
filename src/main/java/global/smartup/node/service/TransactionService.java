package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.EthClient;
import global.smartup.node.mapper.TransactionMapper;
import global.smartup.node.po.Transaction;
import global.smartup.node.util.Common;
import global.smartup.node.util.MapBuilder;
import global.smartup.node.util.Pagination;
import global.smartup.node.vo.Tx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private MarketService marketService;

    /**
     * 保存pending的交易，等待后续解析tx
     * @param txHash
     * @param type
     */
    public void addPending(String txHash, String userAddress, String type) {
        Transaction tr = transactionMapper.selectByPrimaryKey(txHash);
        if (tr == null) {
            tr = new Transaction();
            tr.setTxHash(txHash);
            tr.setStage(PoConstant.TxStage.Pending);
            tr.setUserAddress(userAddress);
            tr.setType(type);
            tr.setCreateTime(new Date());
            transactionMapper.insert(tr);
        }
    }

    public void modChargeSutFinish(String txHash, boolean isSuccess, BigDecimal sut, Date blockTime) {
        Transaction tr = query(txHash);
        tr.setStage(isSuccess ? PoConstant.TxStage.Success : PoConstant.TxStage.Fail);
        Map<String, Object> detail = MapBuilder.<String, Object>create().put("sut", sut).build();
        tr.setDetail(JSON.toJSONString(detail, SerializerFeature.WriteBigDecimalAsPlain));
        tr.setBlockTime(blockTime);
        transactionMapper.updateByPrimaryKey(tr);
    }

    public void modChargeEthFinish(String txHash, boolean isSuccess, BigDecimal eth, Date blockTime) {
        Transaction tr = query(txHash);
        tr.setStage(isSuccess ? PoConstant.TxStage.Success : PoConstant.TxStage.Fail);
        Map<String, Object> detail = MapBuilder.<String, Object>create().put("eth", eth).build();
        tr.setDetail(JSON.toJSONString(detail, SerializerFeature.WriteBigDecimalAsPlain));
        tr.setBlockTime(blockTime);
        transactionMapper.updateByPrimaryKey(tr);
    }

    public void modWithdrawSutFinish(String txHash, boolean isSuccess, BigDecimal sut, Date blockTime) {
        Transaction tr = query(txHash);
        tr.setStage(isSuccess ? PoConstant.TxStage.Success : PoConstant.TxStage.Fail);
        Map<String, Object> detail = MapBuilder.<String, Object>create().put("sut", sut).build();
        tr.setDetail(JSON.toJSONString(detail, SerializerFeature.WriteBigDecimalAsPlain));
        tr.setBlockTime(blockTime);
        transactionMapper.updateByPrimaryKey(tr);
    }

    public void modWithdrawEthFinish(String txHash, boolean isSuccess, BigDecimal eth, Date blockTime) {
        Transaction tr = query(txHash);
        tr.setStage(isSuccess ? PoConstant.TxStage.Success : PoConstant.TxStage.Fail);
        Map<String, Object> detail = MapBuilder.<String, Object>create().put("eth", eth).build();
        tr.setDetail(JSON.toJSONString(detail, SerializerFeature.WriteBigDecimalAsPlain));
        tr.setBlockTime(blockTime);
        transactionMapper.updateByPrimaryKey(tr);
    }

    public void addCreateMarket(String txHash, String marketId, String userAddress) {
        Transaction tr = query(txHash);
        if (tr == null) {
            add(txHash, PoConstant.TxStage.Pending, PoConstant.Transaction.Type.CreateMarket,
                    null, null, new Date(), null);
        } else {
            log.error("Add create market transaction error, repeat txHash = {}" , txHash);
        }
    }

    public void modCreateMarketFinish(String txHash, String stage, String userAddress, String marketId,
                                      String marketAddress, BigDecimal sutAmount, Date blockTime) {
        Transaction tr = query(txHash);
        HashMap<String, Object> map = new HashMap<>();
        map.put("marketId", marketId);
        map.put("marketAddress", marketAddress);
        map.put("sut", sutAmount);
        if (tr == null) {
            add(txHash, stage, PoConstant.Transaction.Type.CreateMarket, marketAddress, map,
                    new Date(), blockTime);
        } else {
            tr.setStage(stage);
            tr.setUserAddress(userAddress);
            tr.setDetail(JSON.toJSONString(map, SerializerFeature.WriteBigDecimalAsPlain));
            tr.setBlockTime(blockTime);
            transactionMapper.updateByPrimaryKey(tr);
        }
    }

    public void addTrade(String txHash, String type, String userAddress, String marketId, String marketAddress,
                         BigDecimal sut, BigDecimal ct) {
        Transaction tr = query(txHash);
        if (tr == null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("sut", sut);
            map.put("ct", ct);
            add(txHash, PoConstant.TxStage.Pending, type, userAddress, map, new Date(),
                    null);
        } else {
            log.error("Add trade transaction error, repeat txHash = {}" , txHash);
        }
    }

    public void modTradeFinish(String txHash, String stage, String type, String userAddress, String marketId,
                               String marketAddress, BigDecimal sut, BigDecimal ct, Date blockTime) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("marketId", marketId);
        map.put("marketAddress", marketAddress);
        map.put("sut", sut);
        map.put("ct", ct);
        Transaction ts = query(txHash);
        if (ts == null) {
            add(txHash, stage, type, userAddress, map, new Date(), blockTime);
        } else {
            ts.setStage(stage);
            ts.setUserAddress(userAddress);
            ts.setDetail(JSON.toJSONString(map, SerializerFeature.WriteBigDecimalAsPlain));
            ts.setBlockTime(blockTime);
            transactionMapper.updateByPrimaryKey(ts);
        }
    }

    private void add(String txHash, String stage, String type, String userAddress, HashMap<String, Object> detail,
                     Date createTime, Date blockTime) {
        Transaction tr = new Transaction();
        tr.setTxHash(txHash);
        tr.setStage(stage);
        tr.setType(type);
        tr.setUserAddress(userAddress);
        if (detail != null) {
            tr.setDetail(JSON.toJSONString(detail, SerializerFeature.WriteBigDecimalAsPlain));
        }
        tr.setCreateTime(createTime);
        tr.setBlockTime(blockTime);
        transactionMapper.insert(tr);
    }

    public boolean isLastTradeTransactionInSegment(String marketAddress, Date time, String segment) {
        Date end = Common.getEndTimeInSegment(segment, time);
        Example example = new Example(Transaction.class);
        example.createCriteria()
                .andEqualTo("marketAddress", marketAddress)
                .andGreaterThan("blockTime", time).andLessThanOrEqualTo("blockTime", end);
        int count = transactionMapper.selectCountByExample(example);
        return count == 0;
    }

    public boolean isTxHashHandled(String txHash) {
        Transaction t = transactionMapper.selectByPrimaryKey(txHash);
        if (t == null) {
            return false;
        }
        if (t.getStage().equals(PoConstant.TxStage.Pending)) {
            return false;
        }
        return true;
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
        list.forEach(tr -> {
            Tx tx = new Tx();
            BeanUtils.copyProperties(tr, tx, "detail");
            if (StringUtils.isNotBlank(tr.getDetail())) {
                HashMap map = JSON.parseObject(tr.getDetail(), HashMap.class, Feature.UseBigDecimal);
                tx.setDetail(map);
                ret.add(tx);
            }
            // marketIds.add(ts.getMarketId());
        });
        List<String> marketNames = marketService.queryNames(marketIds);
        for (int i = 0; i < ret.size(); i++) {
            ret.get(i).setMarketName(marketNames.get(i));
        }
        return ret;
    }
}
