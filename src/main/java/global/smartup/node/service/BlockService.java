package global.smartup.node.service;

import global.smartup.node.Config;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.info.BuyCTInfo;
import global.smartup.node.eth.info.Constant;
import global.smartup.node.eth.info.CreateMarketInfo;
import global.smartup.node.eth.info.SellCTInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Date;

@Service
public class BlockService {

    private static final Logger log = LoggerFactory.getLogger(BlockService.class);

    private static BigInteger ParseBlockNumber = null;

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
                    log.info("Handle create market txHash = {}", tx.getHash());

                    CreateMarketInfo info = new CreateMarketInfo();
                    info.parseTransaction(tx);
                    TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
                    if (ethClient.isTransactionFail(receipt)) {
                        return;
                    }
                    info.parseTransactionReceipt(receipt);

                    // save market
                    marketService.updateCreateByChain(info);
                }

                // call buy CT
                if (BuyCTInfo.isBuyCTTransaction(input, config.ethSmartupContract)) {
                    log.info("Handle buy CT txHash = {}", tx.getHash());

                    if (tradeService.isTxHashExist(tx.getHash())) {
                        return;
                    }

                    BuyCTInfo info = new BuyCTInfo();
                    info.parseTransaction(tx);
                    TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
                    if (ethClient.isTransactionFail(receipt)) {
                        return;
                    }
                    info.parseTransactionReceipt(receipt);
                    info.setBlockTime(new Date(block.getTimestamp().longValue() * 1000));

                    // save transaction
                    tradeService.saveBuyTxByChain(info);

                    // update kline
                    klineNodeService.updateNodeForBuyTxByChain(info);

                    // update market data
                    marketService.updateBuyTradeByChain(info);

                    // update ct account
                    ctAccountService.updateFromChain(info.getEventMarketAddress(), info.getEventUserAddress());
                }

                // TODO
                // proposal

            }
        }

        // call CT
        if (marketService.isMarketExist(to)) {
            String input = tx.getInput();
            // call sell CT
            if (input.startsWith(Constant.CT.Sell)) {
                log.info("Handle sell CT txHash = {}", tx.getHash());

                if (tradeService.isTxHashExist(tx.getHash())) {
                    return;
                }

                SellCTInfo info = new SellCTInfo();
                info.parseTransaction(tx);
                TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
                if (ethClient.isTransactionFail(receipt)) {
                    return;
                }
                info.parseTransactionReceipt(receipt);
                info.setBlockTime(new Date(block.getTimestamp().longValue() * 1000));

                // save transaction
                tradeService.saveSellTxByChain(info);

                // update kline
                klineNodeService.updateNodeForSellTxByChain(info);

                // update market data
                marketService.updateSellTradeByChain(info);

                // update ct account
                ctAccountService.updateFromChain(info.getEventMarketAddress(), info.getEventUserAddress());
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

}
