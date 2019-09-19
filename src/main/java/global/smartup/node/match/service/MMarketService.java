package global.smartup.node.match.service;

import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.MarketMapper;
import global.smartup.node.po.Market;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MMarketService {

    @Autowired
    private MarketMapper marketMapper;

    public List<Market> loadAllMarket() {
        List<Market> markets = marketMapper.selectAll();
        return markets.stream().filter(m -> PoConstant.Market.Status.Open.equals(m.getStatus())).collect(Collectors.toList());
    }

}
