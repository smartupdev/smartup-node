package global.smartup.node.eth;

import global.smartup.node.Config;
import global.smartup.node.eth.info.BuyCTInfo;
import global.smartup.node.eth.info.CreateMarketInfo;
import global.smartup.node.eth.info.Constant;
import global.smartup.node.eth.info.SellCTInfo;
import global.smartup.node.service.DictService;
import global.smartup.node.service.KlineNodeService;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Date;

@Component
public class BlockListener {

    private static final Logger log = LoggerFactory.getLogger(BlockListener.class);

    private static BigInteger ParseBlockNumber = null;

    @Autowired
    private Config config;

    @Autowired
    private EthClient ethClient;

    @Autowired
    private SmartupClient smartupClient;

    @Autowired
    private DictService dictService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private KlineNodeService klineNodeService;

    public void start() {
        BigInteger current = ethClient.getLastBlockNumber();
        if (current == null) {
            return;
        }
        if (ParseBlockNumber == null) {
            if (config.profilesActive.equals("dev")) {
                ParseBlockNumber = current;
            } else {
                ParseBlockNumber = dictService.getParseBlockNumber();
            }
        }
        if (ParseBlockNumber.compareTo(current) > 0) {
            return;
        }

        log.info("Current parse block {}", ParseBlockNumber);
        EthBlock.Block block = ethClient.getBlockByNumber(ParseBlockNumber, true);
        if (block == null) {
            return;
        }

        // parse
        parseBlock(block);

        // add
        ParseBlockNumber = ParseBlockNumber.add(BigInteger.ONE);
        dictService.saveParseBlockNumber(ParseBlockNumber);
    }

    public void parseBlock(EthBlock.Block block) {
        if (block == null) {
            return;
        }
        for (EthBlock.TransactionResult tx : block.getTransactions()) {
            Transaction transaction = (Transaction) tx.get();
            parseTx(block, transaction);
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
