package global.smartup.node.service;

import global.smartup.node.Config;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.info.BuyCTInfo;
import global.smartup.node.eth.info.Constant;
import global.smartup.node.eth.info.CreateMarketInfo;
import global.smartup.node.eth.info.SellCTInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.Date;

@Service
public class BlockService {

    private static final Logger log = LoggerFactory.getLogger(BlockService.class);

    @Autowired
    private Config config;

    @Autowired
    private EthClient ethClient;

    @Autowired
    private MarketService marketService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private KlineNodeService klineNodeService;

    @Autowired
    private CTAccountService ctAccountService;

    @Autowired
    private NotificationService notificationService;


    public void parseBlock(EthBlock.Block block) {
        if (block == null) {
            return;
        }
        try {
            for (EthBlock.TransactionResult tx : block.getTransactions()) {
                Transaction transaction = (Transaction) tx.get();
                parseTx(block, transaction);
            }
        } catch (Exception e) {
            log.error("Handle block exception, number = {}", block.getNumber().toString());
            log.error(e.getMessage(), e);
        }
    }

    public void parseTx(EthBlock.Block block, Transaction tx) {
        if (tx == null || tx.getTo() == null) {
            return;
        }

        String to = tx.getTo();

        // call SUT
        if (to.equals(config.ethSutContract)) {
            String input = tx.getInput();
            if (input.startsWith(Constant.SUT.ApproveAndCall)) {

                //  call create market
                if (input.endsWith(CreateMarketInfo.ByteLastFlag)) {
                    handleCreateMarket(tx);
                }

                // call buy CT
                if (BuyCTInfo.isBuyCTTransaction(input, config.ethSmartupContract)) {
                    handleBuyCT(block, tx);
                }

                // TODO
                // proposal

            }
        }

        // call CT
        if (marketService.isMarketAddressInCache(to)) {
            String input = tx.getInput();

            // call sell CT
            if (input.startsWith(Constant.CT.Sell)) {
                handleSellCT(block, tx);
            }

        }

        // TODO
        // call smartup
        if (to.equals(config.ethSmartupContract)) {
            // flag

            // flag vote

            // proposal
        }

    }


    private void handleCreateMarket(Transaction tx) {
        log.info("Handle create market txHash = {}", tx.getHash());

        String from = Keys.toChecksumAddress(tx.getFrom());
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());

        if (ethClient.isTransactionFail(receipt)) {
            // tx fail

            // update market
            marketService.updateCreateFailByChain(tx.getHash(), from);

            // send ntfc
            notificationService.sendMarketCreateFinish(tx.getHash(), false, from, null);

        } else {
            // tx success

            CreateMarketInfo info = new CreateMarketInfo();
            info.parseTransaction(tx);
            info.parseTransactionReceipt(receipt);

            // update market
            marketService.updateCreateByChain(info);

            // send  ntfc
            notificationService.sendMarketCreateFinish(info.getTxHash(), true, from,  info.getEventMarketAddress());
        }


    }

    private void handleBuyCT(EthBlock.Block block, Transaction tx) {
        log.info("Handle buy CT txHash = {}", tx.getHash());

        if (tradeService.isTxHashHandled(tx.getHash())) {
            return;
        }

        Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
        String from = Keys.toChecksumAddress(tx.getFrom());
        BuyCTInfo info = new BuyCTInfo();
        info.parseTransaction(tx);
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());

        if (ethClient.isTransactionFail(receipt)) {
            // tx fail

            // save
            tradeService.saveFailTradeTxByChain(tx.getHash(), PoConstant.Trade.Type.Buy, from,
                    info.getInputMarketAddress(), info.getInputSUT(), info.getInputCT(), blockTime);

            // send ntfc
            notificationService.sendTradeFinish(info.getTxHash(), false, from, PoConstant.Trade.Type.Buy,
                    info.getInputMarketAddress(), info.getInputSUT(), info.getInputCT());

        } else {
            // tx success

            info.parseTransactionReceipt(receipt);
            info.setBlockTime(blockTime);

            // save transaction
            tradeService.saveBuyTxByChain(info);

            // update kline
            klineNodeService.updateNodeForBuyTxByChain(info);

            // update market data
            marketService.updateBuyTradeByChain(info);

            // update ct account
            ctAccountService.updateFromChain(info.getEventMarketAddress(), info.getEventUserAddress());

            // send ntfc
            notificationService.sendTradeFinish(info.getTxHash(), true, from, PoConstant.Trade.Type.Buy,
                    info.getEventMarketAddress(), info.getEventSUT(), info.getEventCT());
        }

    }

    private void handleSellCT(EthBlock.Block block, Transaction tx) {
        log.info("Handle sell CT txHash = {}", tx.getHash());

        if (tradeService.isTxHashHandled(tx.getHash())) {
            return;
        }

        Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
        String from = Keys.toChecksumAddress(tx.getFrom());
        String to = Keys.toChecksumAddress(tx.getTo());
        SellCTInfo info = new SellCTInfo();
        info.parseTransaction(tx);
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());

        if (ethClient.isTransactionFail(receipt)) {
            // tx fail

            // save
            tradeService.saveFailTradeTxByChain(tx.getHash(), PoConstant.Trade.Type.Sell, from, to, null,
                    info.getInputCT(), blockTime);

            // send ntfc
            notificationService.sendTradeFinish(tx.getHash(), false, from, PoConstant.Trade.Type.Sell, to,
                    null, info.getInputCT());
        } else {
            // tx success

            info.parseTransactionReceipt(receipt);
            info.setBlockTime(blockTime);

            // save transaction
            tradeService.saveSellTxByChain(info);

            // update kline
            klineNodeService.updateNodeForSellTxByChain(info);

            // update market data
            marketService.updateSellTradeByChain(info);

            // update ct account
            ctAccountService.updateFromChain(info.getEventMarketAddress(), info.getEventUserAddress());

            // send ntfc
            notificationService.sendTradeFinish(info.getTxHash(), true, from, PoConstant.Trade.Type.Sell, to,
                    info.getEventSUT(), info.getEventCT());
        }

    }

}
