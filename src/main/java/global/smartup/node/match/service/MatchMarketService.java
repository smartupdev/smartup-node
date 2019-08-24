package global.smartup.node.match.service;

import global.smartup.node.mapper.MarketMapper;
import global.smartup.node.po.Market;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchMarketService {

    @Autowired
    private MarketMapper marketMapper;

    public List<Market> loadAllMarket() {
        return marketMapper.selectAll();
    }

}
