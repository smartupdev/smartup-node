package global.smartup.node.service;

import global.smartup.node.Config;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.info.*;
import global.smartup.node.po.Market;
import global.smartup.node.po.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.Date;
import java.util.List;

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
    private ProposalService proposalService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionService transactionService;


    public void parseBlock(EthBlock.Block block) {
        if (block == null) {
            return;
        }
        try {
            for (EthBlock.TransactionResult tx : block.getTransactions()) {
                Transaction transaction = (Transaction) tx.get();
                parseTx(transaction);
            }
        } catch (Exception e) {
            log.error("Handle block exception, number = {}", block.getNumber().toString());
            log.error(e.getMessage(), e);
        }
    }

    // 解析区块中的交易，放置到数据库中，作为pending待处理
    private void parseTx(Transaction tx) {
        if (tx == null || tx.getTo() == null) {
            return;
        }

        String to = tx.getTo();

        // call SUT
        if (to.equals(config.ethSutContract)) {
            String input = tx.getInput();
            if (input.startsWith(Constant.SUT.ApproveAndCall)) {

                //  call create market
                if (input.endsWith(MarketCreateInfo.ByteLastFlag)) {
                    // handleCreateMarket(block, tx);
                    transactionService.addPending(tx.getHash(), PoConstant.Transaction.Type.CreateMarket);

                }

                // call buy CT
                if (CTBuyInfo.isBuyCTTransaction(input, config.ethSmartupContract)) {
                    // handleBuyCT(block, tx);
                    transactionService.addPending(tx.getHash(), PoConstant.Transaction.Type.BuyCT);
                }

                // appeal

            }
        }

        // call CT
        if (marketService.isMarketAddressInCache(to)) {
            String input = tx.getInput();

            // call sell CT
            if (input.startsWith(Constant.CT.Sell)) {
                // handleSellCT(block, tx);
                transactionService.addPending(tx.getHash(), PoConstant.Transaction.Type.SellCT);
            }

            // // call sut proposal
            // if (input.startsWith(Constant.CT.SutProposal)) {
            //     handleSutProposal(block, tx);
            // }
            //
            // // call sut proposal vote
            // if (input.startsWith(Constant.CT.SutProposalVote)) {
            //     handleSutProposalVote(block, tx);
            // }
            //
            // // call sut proposal finish
            // if (input.startsWith(Constant.CT.SutProposalFinish)) {
            //     handleSutProposalFinish(block, tx);
            // }
            //
            // //  call suggest proposal
            // if (input.startsWith(Constant.CT.SuggestProposal)) {
            //     handleSuggestProposal(block, tx);
            // }
            //
            // // call suggest proposal vote
            // if (input.startsWith(Constant.CT.SuggestProposalVote)) {
            //     handleSuggestProposalVote(block, tx);
            // }
            //
            // // call suggest proposal finish
            // if (input.startsWith(Constant.CT.SuggestProposalFinish)) {
            //     handleSuggestProposalFinish(block, tx);
            // }

        }

        // // call SmartUp
        // if (to.equals(config.ethSmartupContract)) {
        //     // flag
        //
        //     // flag vote
        //
        // }

    }

    // 处理数据库中pending中的交易
    public void handlePendingTransaction() {
        List<global.smartup.node.po.Transaction> transactionList = transactionService.queryPendingList();
        for (global.smartup.node.po.Transaction tr : transactionList) {
            Transaction tx = ethClient.getTx(tr.getTxHash());
            TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
            EthBlock.Block block = ethClient.getBlockByNumber(tx.getBlockNumber(), false);
            if (tx == null || receipt == null || block == null) {
                // 有可能节点还没有同步到收据，放置到下一次处理
                continue;
            }
            Date blockTime = new Date(block.getTimestamp().longValue() * 1000);

            if (PoConstant.Transaction.Type.CreateMarket.equals(tr.getType())) {
                handleCreateMarket(blockTime, tx, receipt);
            }

            if (PoConstant.Transaction.Type.BuyCT.equals(tr.getType())) {
                handleBuyCT(blockTime, tx, receipt);
            }

            if (PoConstant.Transaction.Type.SellCT.equals(tr.getType())) {
                handleSellCT(blockTime, tx, receipt);
            }
        }
    }


    private void handleCreateMarket(Date blockTime, Transaction tx, TransactionReceipt receipt) {

        if (transactionService.isTxHashHandled(tx.getHash())) {
            return;
        }

        log.info("Handle create market txHash = {}", tx.getHash());


        String from = Keys.toChecksumAddress(tx.getFrom());

        Market market = marketService.queryCurrentCreating(from);
        if (market == null) {
            log.error("Can not find creating market by user address = {}, tx hash = {}", from, tx.getHash());
            return;
        }
        MarketCreateInfo info = new MarketCreateInfo();
        info.parseTransaction(tx);

        if (ethClient.isTransactionFail(receipt)) {
            // update market
            marketService.updateCreateFailByChain(tx.getHash(), from, info.getInputAmount());

            // update ts
            transactionService.modCreateMarketFinish(info.getTxHash(), PoConstant.TxStage.Fail,
                    from, market.getMarketId(), null, info.getInputAmount(), blockTime);

            // send ntfc
            notificationService.sendMarketCreateFinish(tx.getHash(), false, market.getMarketId(), from, null, market.getName());

        } else {
            // tx success

            info.parseTransactionReceipt(receipt);

            // update market
            marketService.updateCreateByChain(info);

            // update ts
            transactionService.modCreateMarketFinish(info.getTxHash(), PoConstant.TxStage.Success,
                    info.getEventCreatorAddress(), market.getMarketId(), info.getEventMarketAddress(),
                    info.getEventAmount(), blockTime);

            // send  ntfc
            notificationService.sendMarketCreateFinish(info.getTxHash(), true, market.getMarketId(), from,
                    info.getEventMarketAddress(), market.getName());
        }


    }

    private void handleBuyCT(Date blockTime, Transaction tx, TransactionReceipt receipt) {

        if (transactionService.isTxHashHandled(tx.getHash())) {
            return;
        }

        log.info("Handle buy CT txHash = {}", tx.getHash());

        String from = Keys.toChecksumAddress(tx.getFrom());

        CTBuyInfo info = new CTBuyInfo();
        info.parseTransaction(tx);

        Market market = marketService.queryByAddress(info.getInputMarketAddress());
        if (market == null) {
            log.error("Can not find market address = {}", info.getInputMarketAddress());
            return;
        }

        if (ethClient.isTransactionFail(receipt)) {
            // save
            tradeService.saveFailTradeTxByChain(tx.getHash(), PoConstant.Trade.Type.Buy, from,
                    info.getInputMarketAddress(), info.getInputSUT(), info.getInputCT(), blockTime);

            // update ts
            transactionService.modTradeFinish(tx.getHash(), PoConstant.TxStage.Fail, PoConstant.Transaction.Type.BuyCT,
                    from, market.getMarketId(), market.getMarketAddress(), info.getInputSUT(), info.getInputCT(), blockTime);

            // send ntfc
            notificationService.sendTradeFinish(info.getTxHash(), false, from, PoConstant.Trade.Type.Buy,
                    market.getMarketId(), info.getInputMarketAddress(), market.getName(), info.getInputSUT(), info.getInputCT());

        } else {
            // tx success

            info.parseTransactionReceipt(receipt);
            info.setBlockTime(blockTime);

            // save transaction
            tradeService.saveBuyTxByChain(info);

            // update ts
            transactionService.modTradeFinish(tx.getHash(), PoConstant.TxStage.Success, PoConstant.Transaction.Type.BuyCT,
                    from, market.getMarketId(), market.getMarketAddress(), info.getEventSUT(), info.getEventCT(), blockTime);

            // update ct account
            ctAccountService.updateFromChain(info.getEventMarketAddress(), info.getEventUserAddress());

            // update kline
            klineNodeService.updateNodeForBuyTxByChain(info);

            // update market data
            marketService.updateBuyTradeByChain(info);

            // send ntfc
            notificationService.sendTradeFinish(info.getTxHash(), true, from, PoConstant.Trade.Type.Buy,
                    market.getMarketId(), info.getEventMarketAddress(), market.getName(), info.getEventSUT(), info.getEventCT());
        }

    }

    private void handleSellCT(Date blockTime, Transaction tx, TransactionReceipt receipt) {

        if (transactionService.isTxHashHandled(tx.getHash())) {
            return;
        }

        log.info("Handle sell CT txHash = {}", tx.getHash());

        String from = Keys.toChecksumAddress(tx.getFrom());
        String to = Keys.toChecksumAddress(tx.getTo());
        Market market = marketService.queryByAddress(to);
        if (market == null) {
            log.error("Can not find market address = {}", to);
            return;
        }

        CTSellInfo info = new CTSellInfo();
        info.parseTransaction(tx);

        if (ethClient.isTransactionFail(receipt)) {
            // save
            tradeService.saveFailTradeTxByChain(tx.getHash(), PoConstant.Trade.Type.Sell, from, to, null,
                    info.getInputCT(), blockTime);

            // update ts
            transactionService.modTradeFinish(tx.getHash(), PoConstant.TxStage.Fail, PoConstant.Transaction.Type.SellCT,
                    from, market.getMarketId(), to, info.getEventSUT(), info.getEventCT(), blockTime);

            // send ntfc
            notificationService.sendTradeFinish(tx.getHash(), false, from, PoConstant.Trade.Type.Sell,
                    market.getMarketId(), to, market.getName(),null, info.getInputCT());
        } else {
            info.parseTransactionReceipt(receipt);
            info.setBlockTime(blockTime);

            // save transaction
            tradeService.saveSellTxByChain(info);

            // update ts
            transactionService.modTradeFinish(tx.getHash(), PoConstant.TxStage.Success, PoConstant.Transaction.Type.SellCT,
                    from, market.getMarketId(), to, info.getEventSUT(), info.getEventCT(), blockTime);

            // update ct account
            ctAccountService.updateFromChain(info.getEventMarketAddress(), info.getEventUserAddress());

            // update kline
            klineNodeService.updateNodeForSellTxByChain(info);

            // update market data
            marketService.updateSellTradeByChain(info);

            // send ntfc
            notificationService.sendTradeFinish(info.getTxHash(), true, from, PoConstant.Trade.Type.Sell,
                    market.getMarketId(), to, market.getName(), info.getEventSUT(), info.getEventCT());
        }

    }

    private void handleSutProposal(EthBlock.Block block, Transaction tx) {
        log.info("Handle proposal txHash = {}", tx.getHash());

        Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
        String from = Keys.toChecksumAddress(tx.getFrom());
        String to = Keys.toChecksumAddress(tx.getTo());
        ProposalSutCreateInfo info = new ProposalSutCreateInfo();
        info.parseTransaction(tx);
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());

        // query creating proposal
        Proposal proposal = proposalService.queryEditingProposal(from, to, PoConstant.Proposal.Type.Sut);
        if (proposal == null) {
            return;
        }

        if (ethClient.isTransactionFail(receipt)) {
            // tx fail

            // save
            proposalService.updateSutProposalCreatedFromChain(tx.getHash(), false, from, to, info.getInputSutAmount(), blockTime);

            // send ntfc
            notificationService.sendProposalCreated(tx.getHash(), false, from, to, proposal.getProposalId(),
                    proposal.getType(), info.getInputSutAmount());
        } else {
            // tx success

            // save
            proposalService.updateSutProposalCreatedFromChain(tx.getHash(), true, from, to, info.getInputSutAmount(), blockTime);

            // send proposal ntfc
            notificationService.sendProposalCreated(tx.getHash(), true, from, to, proposal.getProposalId(),
                    proposal.getType(), info.getInputSutAmount());

            // TODO
            //  send vote ntfc
        }


    }


    private void handleSutProposalVote(EthBlock.Block block, Transaction tx) {
        log.info("Handle sut proposal vote txHash = {}", tx.getHash());

        Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
        String from = Keys.toChecksumAddress(tx.getFrom());
        String to = Keys.toChecksumAddress(tx.getTo());
        ProposalSutVoteInfo info = new ProposalSutVoteInfo();
        info.parseTransaction(tx);
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());

        Proposal proposal = proposalService.queryLastCreated(to, PoConstant.Proposal.Type.Sut);
        if (proposal == null) {
            return;
        }

        if (ethClient.isTransactionFail(receipt)) {
            // save
            proposalService.updateSutProposalVoteFromChain(tx.getHash(), false, from, to, info.getInputVote(), blockTime);

            // send ntfc
            notificationService.sendProposalSutVoteFinish(tx.getHash(), false, from, to, proposal.getProposalId(), info.getInputVote());

        } else {
            // save
            proposalService.updateSutProposalVoteFromChain(tx.getHash(), true, from, to, info.getInputVote(), blockTime);

            // send ntfc
            notificationService.sendProposalSutVoteFinish(tx.getHash(), true, from, to, proposal.getProposalId(), info.getInputVote());
        }
    }


    private void handleSutProposalFinish(EthBlock.Block block, Transaction tx) {
        log.info("Handle sut proposal finish = {}", tx.getHash());

        Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
        String from = Keys.toChecksumAddress(tx.getFrom());
        String to = Keys.toChecksumAddress(tx.getTo());
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());

        Proposal proposal = proposalService.queryLastCreated(to, PoConstant.Proposal.Type.Sut);
        if (proposal == null || !proposal.getIsFinished()) {
            return;
        }
        ProposalSutFinishInfo info = new ProposalSutFinishInfo();
        info.parseTransactionReceipt(receipt);

        if (ethClient.isTransactionFail(receipt)) {
            // update proposal
            proposalService.updateSutProposalFinishFromChain(tx.getHash(), false, from, to, null);

            // send ntfc
            notificationService.sendProposalSutFinish(tx.getHash(), false, from, to, proposal.getProposalId(), null);

        } else {
            // update proposal
            proposalService.updateSutProposalFinishFromChain(tx.getHash(), true, from, to, info.getEvenIsAgree());

            // send ntfc
            notificationService.sendProposalSutFinish(tx.getHash(), true, from, to, proposal.getProposalId(), info.getEvenIsAgree());
        }
    }

    private void handleSuggestProposal(EthBlock.Block block, Transaction tx) {
        log.info("Handle suggest proposal finish tx hash = {}", tx.getHash());

        Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
        String from = Keys.toChecksumAddress(tx.getFrom());
        String to = Keys.toChecksumAddress(tx.getTo());
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());

        Proposal proposal = proposalService.queryEditingProposal(from, to, PoConstant.Proposal.Type.Suggest);
        if (proposal == null) {
            log.error("Can not find creating suggest proposal tx hash = {}, user = {}, market = {}", to, from);
            return;
        }
        if (!proposal.getIsFinished()) {
            return;
        }

        ProposalSuggestCreateInfo info = new ProposalSuggestCreateInfo();
        info.parseTransactionReceipt(receipt);

        if (ethClient.isTransactionFail(receipt)) {
            // save
            proposalService.updateSuggestProposalCreatedFromChain(tx.getHash(), false, from, to, null, blockTime);

            // send ntfc
            notificationService.sendProposalSuggestVoteFinsh(tx.getHash(), false, from, to, proposal.getProposalId(), null);

        } else {
            // save
            proposalService.updateSuggestProposalCreatedFromChain(tx.getHash(), true, from, to, info.getEventProposalId(), blockTime);

            // send ntfc
            notificationService.sendProposalSuggestVoteFinsh(tx.getHash(), true, from, to, proposal.getProposalId(), info.getEventProposalId());
        }

    }

    private void handleSuggestProposalVote(EthBlock.Block block, Transaction tx) {
        log.info("Handle suggest proposal vote finish = {}", tx.getHash());

        Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
        String from = Keys.toChecksumAddress(tx.getFrom());
        String to = Keys.toChecksumAddress(tx.getTo());
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());



    }

    private void handleSuggestProposalFinish(EthBlock.Block block, Transaction tx) {
        log.info("Handle suggest proposal finish finish = {}", tx.getHash());

        Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
        String from = Keys.toChecksumAddress(tx.getFrom());
        String to = Keys.toChecksumAddress(tx.getTo());
        TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());


    }

}
