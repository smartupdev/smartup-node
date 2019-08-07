package global.smartup.node.service.block;

import global.smartup.node.constant.BuConstant;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.constract.event.FirstStageBuyEvent;
import global.smartup.node.eth.constract.func.FirstStageBuyFunc;
import global.smartup.node.po.Market;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.NotificationService;
import global.smartup.node.service.TradeService;
import global.smartup.node.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.Date;

@Service
public class BlockTradeService {

    private static final Logger log = LoggerFactory.getLogger(BlockTradeService.class);

    @Autowired
    private EthClient ethClient;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NotificationService notificationService;

    public void handleFirstStageBuy(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        if (transactionService.isTxHashHandled(tx.getHash())) {
            return;
        }
        FirstStageBuyFunc func = FirstStageBuyFunc.parse(tx);
        if (func == null) {
            return;
        }
        Market market = marketService.queryByAddress(func.getMarketAddress());
        if (market == null) {
            return;
        }

        boolean isSuccess = false;
        if (ethClient.isTransactionSuccess(receipt)) {
            FirstStageBuyEvent event = FirstStageBuyEvent.parse(receipt);
            isSuccess = true;
        }

        // update tr
        transactionService.modFirstBuyFinish(tx.getHash(), isSuccess, market.getMarketId(), market.getName(),
            func.getCtCount(), market.getCtPrice().setScale(BuConstant.DefaultScale), blockTime);

        // update trade
        tradeService.modFirstBuyFinish(tx.getHash(), isSuccess);

        // send notification
        notificationService.sendFirstStageBuyFinish(tx.getHash(), isSuccess, func.getUserAddress(), market.getMarketId(),
            market.getName(), func.getCtCount(), market.getCtPrice().setScale(BuConstant.DefaultScale));
    }

}
