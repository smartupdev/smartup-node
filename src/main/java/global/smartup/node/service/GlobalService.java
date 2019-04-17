package global.smartup.node.service;

import global.smartup.node.vo.GlobalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalService {

    private static final Logger log = LoggerFactory.getLogger(GlobalService.class);

    @Autowired
    private MarketService marketService;

    @Autowired
    private PostService postService;

    @Autowired
    private TradeService tradeService;

    public GlobalData queryGlobalData() {
        GlobalData data = new GlobalData();
        data.setMarketCount(marketService.queryMarketCount());
        data.setSutAmount(marketService.queryAllMarketSUTAmount());
        data.setLatelyPostCount(postService.queryLatelyReplyCount());
        return data;
    }


}
