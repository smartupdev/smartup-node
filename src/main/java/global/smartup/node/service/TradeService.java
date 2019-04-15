package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.info.BuyCTInfo;
import global.smartup.node.eth.info.SellCTInfo;
import global.smartup.node.mapper.TradeMapper;
import global.smartup.node.po.Trade;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradeMapper tradeMapper;


    public void saveBuyTxByChain(BuyCTInfo info) {
        if (info == null || query(info.getTxHash()) != null) {
            return;
        }
        Trade trade = new Trade();
        trade.setTxHash(info.getTxHash());
        trade.setMarketAddress(info.getEventMarketAddress());
        trade.setUserAddress(info.getEventUserAddress());
        trade.setStage(PoConstant.Trade.Stage.Success);
        trade.setCreateTime(new Date());
        trade.setBlockTime(info.getBlockTime());
        trade.setType(PoConstant.Trade.Type.Buy);
        trade.setSutOffer(info.getEventSUTOffer());
        trade.setSutAmount(info.getEventSUT());
        trade.setCtAmount(info.getEventCT());
        tradeMapper.insert(trade);
    }

    public void saveSellTxByChain(SellCTInfo info) {
        if (info == null || query(info.getTxHash()) != null) {
            return;
        }
        Trade trade = new Trade();
        trade.setTxHash(info.getTxHash());
        trade.setMarketAddress(info.getEventMarketAddress());
        trade.setUserAddress(info.getEventUserAddress());
        trade.setStage(PoConstant.Trade.Stage.Success);
        trade.setCreateTime(new Date());
        trade.setBlockTime(info.getBlockTime());
        trade.setType(PoConstant.Trade.Type.Sell);
        trade.setSutAmount(info.getEventSUT());
        trade.setCtAmount(info.getEventCT());
        tradeMapper.insert(trade);
    }

    public Trade query(String txHash) {
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


}
