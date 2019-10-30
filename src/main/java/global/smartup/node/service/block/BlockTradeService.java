package global.smartup.node.service.block;

import global.smartup.node.constant.BuConstant;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.ExchangeClient;
import global.smartup.node.eth.constract.event.FirstStageBuyEvent;
import global.smartup.node.eth.constract.func.FirstStageBuyFunc;
import global.smartup.node.match.service.MatchService;
import global.smartup.node.po.*;
import global.smartup.node.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class BlockTradeService {

    private static final Logger log = LoggerFactory.getLogger(BlockTradeService.class);

    @Autowired
    private EthClient ethClient;

    @Autowired
    private ExchangeClient exchangeClient;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CTAccountService ctAccountService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private TradeScanService tradeScanService;


    @Transactional
    public void handleFirstStageBuy(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        if (transactionService.isTxHashHandled(tx.getHash())) {
            return;
        }
        FirstStageBuyFunc func = FirstStageBuyFunc.parse(tx);
        Market market = marketService.queryByAddress(func.getMarketAddress());
        if (market == null) {
            return;
        }

        Trade trade = tradeService.queryFirstBuyTrade(tx.getHash());
        if (trade == null) {
            return;
        }

        boolean isSuccess = false;
        if (ethClient.isTransactionSuccess(receipt)) {
            FirstStageBuyEvent event = FirstStageBuyEvent.parse(receipt);
            isSuccess = true;

            // add ct
            ctAccountService.update(market.getMarketAddress(), func.getUserAddress(), func.getCtCount());
            // sub sut
            BigDecimal sut = func.getCtCount().multiply(market.getCtPrice());
            userAccountService.updateLockSut(func.getUserAddress(), sut.negate());

            // add match order
            matchService.loadFirstStageOrder(market.getMarketId(), new Date(), market.getCtPrice(), func.getCtCount());
            MakePlan plan = tradeService.queryPlan(trade.getTradeId());
            matchService.addSellOrder(market.getMarketId(), func.getUserAddress(), plan.getSellPrice(), func.getCtCount(),
                plan.getTimestamp(), plan.getSign());
            tradeService.updateTrading(trade.getTradeId());
        }

        // update market stage
        marketService.updateStage(market.getMarketAddress());

        // update tr
        transactionService.modFirstBuyFinish(tx.getHash(), isSuccess, market.getMarketId(), market.getName(),
            func.getCtCount(), market.getCtPrice().setScale(BuConstant.DefaultScale), blockTime);

        // update trade
        tradeService.modFirstBuyFinish(tx.getHash(), isSuccess);

        // send notification
        notificationService.sendFirstStageBuyFinish(tx.getHash(), isSuccess, func.getUserAddress(), market.getMarketId(),
            market.getName(), func.getCtCount(), market.getCtPrice().setScale(BuConstant.DefaultScale));

        log.debug("[First stage buy handle] tx hash = {}", tx.getHash());
    }

    @Transactional
    public void handleTrade(Transaction tx, TransactionReceipt receipt, Date blockTime) {

    }

    public void scanTakePlan() {
        TakePlan plan = tradeScanService.queryTopTakePlan();
        if (plan == null) {
            return;
        }
        List<TradeChild> children = tradeScanService.queryChild(plan.getTakePlanId());
        if (plan.getTimes().compareTo(children.size()) != 0) {
            return;
        }
        Trade takeOrder = tradeService.queryById(plan.getTakeTradeId());

    }


    public void sendTrade() {

    }

}
