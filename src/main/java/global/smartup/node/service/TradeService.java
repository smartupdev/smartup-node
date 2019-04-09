package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.SmartupClient;
import global.smartup.node.mapper.TradeMapper;
import global.smartup.node.po.Trade;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private SmartupClient smartupClient;

    public Trade add(Trade trade) {
        trade.setCreateTime(new Date());
        tradeMapper.insert(trade);
        return trade;
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

    public void findTrade() {
        Example example = new Example(Trade.class);
        example.createCriteria().andEqualTo("stage", PoConstant.Trade.Stage.Padding);
        example.orderBy("createTime").asc();
        List<Trade> list = tradeMapper.selectByExample(example);
        for (Trade trade : list) {
            TransactionReceipt receipt = smartupClient.queryReceipt(trade.getTxHash());
            if (receipt == null) {
                continue;
            }
            boolean isFail = smartupClient.isTxFail(receipt);
            if (isFail) {
                trade.setStage(PoConstant.Trade.Stage.Fail);
                tradeMapper.updateByPrimaryKey(trade);
                continue;
            }
            if (PoConstant.Trade.Type.Buy.equals(trade.getType())) {
                Trade chain = smartupClient.getBuyPrice(receipt);
                trade.setUserAddress(chain.getUserAddress());
                trade.setSutOffer(chain.getSutOffer());
                trade.setSutAmount(chain.getSutAmount());
                trade.setCtAmount(chain.getCtAmount());
                trade.setMarketAddress(chain.getMarketAddress());
                trade.setStage(PoConstant.Trade.Stage.Success);
            } else if (PoConstant.Trade.Type.Sell.equals(trade.getType())) {
                Trade chain = smartupClient.getSellPrice(receipt);
                trade.setUserAddress(chain.getUserAddress());
                trade.setSutAmount(chain.getSutAmount());
                trade.setCtAmount(chain.getCtAmount());
                trade.setMarketAddress(chain.getMarketAddress());
                trade.setStage(PoConstant.Trade.Stage.Success);
            } else {
                continue;
            }
            tradeMapper.updateByPrimaryKey(trade);
        }
    }

}
