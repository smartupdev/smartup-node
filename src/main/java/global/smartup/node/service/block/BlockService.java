package global.smartup.node.service.block;

import global.smartup.node.Config;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.constract.Const;
import global.smartup.node.eth.constract.func.AdminWithdrawFunc;
import global.smartup.node.eth.constract.func.WithdrawFunc;
import global.smartup.node.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

@Service
public class BlockService {

    private static final Logger log = LoggerFactory.getLogger(BlockService.class);

    @Autowired
    private Config config;

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

    /**
     * 解析区块中的交易，放置到数据库中，作为pending待处理
     * @param tx
     */
    private void parseTx(Transaction tx) {
        if (tx == null || tx.getTo() == null) {
            return;
        }

        String userAddress = Keys.toChecksumAddress(tx.getFrom());
        String to = tx.getTo();
        String input = tx.getInput();

        // call SUT
        if (to.equals(config.ethSutContract)) {
            // 充值SUT
            if (input.startsWith(Const.SUT.Func.ChargeSut)) {
                transactionService.addPending(tx.getHash(), userAddress, PoConstant.Transaction.Type.ChargeSut);
            }
        }

        // call Exchange
        if (to.equals(config.ethExchangeContract)) {
            // 充值ETH
            if (input.startsWith(Const.Exchange.Func.ChargeEth)) {
                transactionService.addPending(tx.getHash(), userAddress, PoConstant.Transaction.Type.ChargeEth);
            }

            // 提取
            if (input.startsWith(Const.Exchange.Func.Withdraw)) {
                WithdrawFunc func = WithdrawFunc.parse(tx);
                if (func.isWithdrawEth()) {
                    transactionService.addPending(tx.getHash(), userAddress, PoConstant.Transaction.Type.WithdrawEth);
                } else {
                    transactionService.addPending(tx.getHash(), userAddress, PoConstant.Transaction.Type.WithdrawSut);
                }

            }

            // 管理员提取
            if (input.startsWith(Const.Exchange.Func.AdminWithdraw)) {
                AdminWithdrawFunc func = AdminWithdrawFunc.parse(tx);
                if (func.isWithdrawEth()) {
                    transactionService.addPending(tx.getHash(), userAddress, PoConstant.Transaction.Type.AdminWithdrawEth);
                } else {
                    transactionService.addPending(tx.getHash(), userAddress, PoConstant.Transaction.Type.AdminWithdrawSut);
                }
            }

            // 创建市场
            if (input.startsWith(Const.Exchange.Func.CreateMarket)) {
                transactionService.addPending(tx.getHash(), userAddress, PoConstant.Transaction.Type.CreateMarket);
            }

            // 第一阶段买入
            if (input.startsWith(Const.Exchange.Func.FirstStageBuy)) {
                transactionService.addPending(tx.getHash(), userAddress, PoConstant.Transaction.Type.FirstStageBuyCT);
            }
        }

    }


    // private void handleCreateMarket(Date blockTime, Transaction tx, TransactionReceipt receipt) {
    //     if (transactionService.isTxHashHandled(tx.getHash())) {
    //         return;
    //     }
    //
    //     String from = Keys.toChecksumAddress(tx.getFrom());
    //
    //     Market market = marketService.queryCurrentCreating(from);
    //     if (market == null) {
    //         return;
    //     }
    //     MarketCreateInfo info = new MarketCreateInfo();
    //     info.parseTransaction(tx);
    //
    //     if (ethClient.isTransactionFail(receipt)) {
    //         // update market
    //         marketService.updateCreateFailByChain(tx.getHash(), from, info.getInputAmount());
    //
    //         // update ts
    //         transactionService.modCreateMarketFinish(info.getTxHash(), PoConstant.TxStage.Fail,
    //                 from, market.getMarketId(), null, info.getInputAmount(), blockTime);
    //
    //         // send ntfc
    //         notificationService.sendMarketCreateFinish(tx.getHash(), false, market.getMarketId(), from, null, market.getName());
    //
    //     } else {
    //         // tx success
    //
    //         info.parseTransactionReceipt(receipt);
    //
    //         // update market
    //         // marketService.updateCreateByChain(info);
    //
    //         // update ts
    //         transactionService.modCreateMarketFinish(info.getTxHash(), PoConstant.TxStage.Success,
    //                 info.getEventCreatorAddress(), market.getMarketId(), info.getEventMarketAddress(),
    //                 info.getEventAmount(), blockTime);
    //
    //         // send  ntfc
    //         notificationService.sendMarketCreateFinish(info.getTxHash(), true, market.getMarketId(), from,
    //                 info.getEventMarketAddress(), market.getName());
    //     }
    //
    //
    // }
    //
    // private void handleBuyCT(Date blockTime, Transaction tx, TransactionReceipt receipt) {
    //     if (transactionService.isTxHashHandled(tx.getHash())) {
    //         return;
    //     }
    //
    //     String from = Keys.toChecksumAddress(tx.getFrom());
    //
    //     CTBuyInfo info = new CTBuyInfo();
    //     info.parseTransaction(tx);
    //
    //     Market market = marketService.queryByAddress(info.getInputMarketAddress());
    //     if (market == null) {
    //         return;
    //     }
    //
    //     if (ethClient.isTransactionFail(receipt)) {
    //         // save
    //         tradeService.saveFailTradeTxByChain(tx.getHash(), PoConstant.Trade.Type.Buy, from,
    //                 info.getInputMarketAddress(), info.getInputSUT(), info.getInputCT(), blockTime);
    //
    //         // update ts
    //         transactionService.modTradeFinish(tx.getHash(), PoConstant.TxStage.Fail, PoConstant.Transaction.Type.BuyCT,
    //                 from, market.getMarketId(), market.getMarketAddress(), info.getInputSUT(), info.getInputCT(), blockTime);
    //
    //         // send ntfc
    //         notificationService.sendTradeFinish(info.getTxHash(), false, from, PoConstant.Trade.Type.Buy,
    //                 market.getMarketId(), info.getInputMarketAddress(), market.getName(), info.getInputSUT(), info.getInputCT());
    //
    //     } else {
    //         // tx success
    //
    //         info.parseTransactionReceipt(receipt);
    //         info.setBlockTime(blockTime);
    //
    //         // save transaction
    //         tradeService.saveBuyTxByChain(info);
    //
    //         // update ts
    //         transactionService.modTradeFinish(tx.getHash(), PoConstant.TxStage.Success, PoConstant.Transaction.Type.BuyCT,
    //                 from, market.getMarketId(), market.getMarketAddress(), info.getEventSUT(), info.getEventCT(), blockTime);
    //
    //         // update ct account
    //         ctAccountService.updateFromChain(info.getEventMarketAddress(), info.getEventUserAddress());
    //
    //         // update kline
    //         klineNodeService.updateNodeForBuyTxByChain(info);
    //
    //         // update market data
    //         marketService.updateBuyTradeByChain(info);
    //
    //         // send ntfc
    //         notificationService.sendTradeFinish(info.getTxHash(), true, from, PoConstant.Trade.Type.Buy,
    //                 market.getMarketId(), info.getEventMarketAddress(), market.getName(), info.getEventSUT(), info.getEventCT());
    //     }
    //
    // }
    //
    // private void handleSellCT(Date blockTime, Transaction tx, TransactionReceipt receipt) {
    //     if (transactionService.isTxHashHandled(tx.getHash())) {
    //         return;
    //     }
    //
    //     String from = Keys.toChecksumAddress(tx.getFrom());
    //     String to = Keys.toChecksumAddress(tx.getTo());
    //     Market market = marketService.queryByAddress(to);
    //     if (market == null) {
    //         return;
    //     }
    //
    //     CTSellInfo info = new CTSellInfo();
    //     info.parseTransaction(tx);
    //
    //     if (ethClient.isTransactionFail(receipt)) {
    //         // save
    //         tradeService.saveFailTradeTxByChain(tx.getHash(), PoConstant.Trade.Type.Sell, from, to, null,
    //                 info.getInputCT(), blockTime);
    //
    //         // update ts
    //         transactionService.modTradeFinish(tx.getHash(), PoConstant.TxStage.Fail, PoConstant.Transaction.Type.SellCT,
    //                 from, market.getMarketId(), to, info.getEventSUT(), info.getEventCT(), blockTime);
    //
    //         // send ntfc
    //         notificationService.sendTradeFinish(tx.getHash(), false, from, PoConstant.Trade.Type.Sell,
    //                 market.getMarketId(), to, market.getName(),null, info.getInputCT());
    //     } else {
    //         info.parseTransactionReceipt(receipt);
    //         info.setBlockTime(blockTime);
    //
    //         // save transaction
    //         tradeService.saveSellTxByChain(info);
    //
    //         // update ts
    //         transactionService.modTradeFinish(tx.getHash(), PoConstant.TxStage.Success, PoConstant.Transaction.Type.SellCT,
    //                 from, market.getMarketId(), to, info.getEventSUT(), info.getEventCT(), blockTime);
    //
    //         // update ct account
    //         ctAccountService.updateFromChain(info.getEventMarketAddress(), info.getEventUserAddress());
    //
    //         // update kline
    //         klineNodeService.updateNodeForSellTxByChain(info);
    //
    //         // update market data
    //         marketService.updateSellTradeByChain(info);
    //
    //         // send ntfc
    //         notificationService.sendTradeFinish(info.getTxHash(), true, from, PoConstant.Trade.Type.Sell,
    //                 market.getMarketId(), to, market.getName(), info.getEventSUT(), info.getEventCT());
    //     }
    //
    // }
    //
    // private void handleSutProposal(EthBlock.Block block, Transaction tx) {
    //     log.info("Handle proposal txHash = {}", tx.getHash());
    //
    //     Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
    //     String from = Keys.toChecksumAddress(tx.getFrom());
    //     String to = Keys.toChecksumAddress(tx.getTo());
    //     ProposalSutCreateInfo info = new ProposalSutCreateInfo();
    //     info.parseTransaction(tx);
    //     TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
    //
    //     // query creating proposal
    //     Proposal proposal = proposalService.queryEditingProposal(from, to, PoConstant.Proposal.Type.Sut);
    //     if (proposal == null) {
    //         return;
    //     }
    //
    //     if (ethClient.isTransactionFail(receipt)) {
    //         // tx fail
    //
    //         // save
    //         proposalService.updateSutProposalCreatedFromChain(tx.getHash(), false, from, to, info.getInputSutAmount(), blockTime);
    //
    //         // send ntfc
    //         notificationService.sendProposalCreated(tx.getHash(), false, from, to, proposal.getProposalId(),
    //                 proposal.getType(), info.getInputSutAmount());
    //     } else {
    //         // tx success
    //
    //         // save
    //         proposalService.updateSutProposalCreatedFromChain(tx.getHash(), true, from, to, info.getInputSutAmount(), blockTime);
    //
    //         // send proposal ntfc
    //         notificationService.sendProposalCreated(tx.getHash(), true, from, to, proposal.getProposalId(),
    //                 proposal.getType(), info.getInputSutAmount());
    //
    //     }
    //
    //
    // }
    //
    // private void handleSutProposalVote(EthBlock.Block block, Transaction tx) {
    //     log.info("Handle sut proposal vote txHash = {}", tx.getHash());
    //
    //     Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
    //     String from = Keys.toChecksumAddress(tx.getFrom());
    //     String to = Keys.toChecksumAddress(tx.getTo());
    //     ProposalSutVoteInfo info = new ProposalSutVoteInfo();
    //     info.parseTransaction(tx);
    //     TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
    //
    //     Proposal proposal = proposalService.queryLastCreated(to, PoConstant.Proposal.Type.Sut);
    //     if (proposal == null) {
    //         return;
    //     }
    //
    //     if (ethClient.isTransactionFail(receipt)) {
    //         // save
    //         proposalService.updateSutProposalVoteFromChain(tx.getHash(), false, from, to, info.getInputVote(), blockTime);
    //
    //         // send ntfc
    //         notificationService.sendProposalSutVoteFinish(tx.getHash(), false, from, to, proposal.getProposalId(), info.getInputVote());
    //
    //     } else {
    //         // save
    //         proposalService.updateSutProposalVoteFromChain(tx.getHash(), true, from, to, info.getInputVote(), blockTime);
    //
    //         // send ntfc
    //         notificationService.sendProposalSutVoteFinish(tx.getHash(), true, from, to, proposal.getProposalId(), info.getInputVote());
    //     }
    // }
    //
    // private void handleSutProposalFinish(EthBlock.Block block, Transaction tx) {
    //     log.info("Handle sut proposal finish = {}", tx.getHash());
    //
    //     Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
    //     String from = Keys.toChecksumAddress(tx.getFrom());
    //     String to = Keys.toChecksumAddress(tx.getTo());
    //     TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
    //
    //     Proposal proposal = proposalService.queryLastCreated(to, PoConstant.Proposal.Type.Sut);
    //     if (proposal == null || !proposal.getIsFinished()) {
    //         return;
    //     }
    //     ProposalSutFinishInfo info = new ProposalSutFinishInfo();
    //     info.parseTransactionReceipt(receipt);
    //
    //     if (ethClient.isTransactionFail(receipt)) {
    //         // update proposal
    //         proposalService.updateSutProposalFinishFromChain(tx.getHash(), false, from, to, null);
    //
    //         // send ntfc
    //         notificationService.sendProposalSutFinish(tx.getHash(), false, from, to, proposal.getProposalId(), null);
    //
    //     } else {
    //         // update proposal
    //         proposalService.updateSutProposalFinishFromChain(tx.getHash(), true, from, to, info.getEvenIsAgree());
    //
    //         // send ntfc
    //         notificationService.sendProposalSutFinish(tx.getHash(), true, from, to, proposal.getProposalId(), info.getEvenIsAgree());
    //     }
    // }
    //
    // private void handleSuggestProposal(EthBlock.Block block, Transaction tx) {
    //     log.info("Handle suggest proposal finish tx hash = {}", tx.getHash());
    //
    //     Date blockTime = new Date(block.getTimestamp().longValue() * 1000);
    //     String from = Keys.toChecksumAddress(tx.getFrom());
    //     String to = Keys.toChecksumAddress(tx.getTo());
    //     TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
    //
    //     Proposal proposal = proposalService.queryEditingProposal(from, to, PoConstant.Proposal.Type.Suggest);
    //     if (proposal == null) {
    //         log.error("Can not find creating suggest proposal tx hash = {}, user = {}, market = {}", to, from);
    //         return;
    //     }
    //     if (!proposal.getIsFinished()) {
    //         return;
    //     }
    //
    //     ProposalSuggestCreateInfo info = new ProposalSuggestCreateInfo();
    //     info.parseTransactionReceipt(receipt);
    //
    //     if (ethClient.isTransactionFail(receipt)) {
    //         // save
    //         proposalService.updateSuggestProposalCreatedFromChain(tx.getHash(), false, from, to, null, blockTime);
    //
    //         // send ntfc
    //         notificationService.sendProposalSuggestVoteFinsh(tx.getHash(), false, from, to, proposal.getProposalId(), null);
    //
    //     } else {
    //         // save
    //         proposalService.updateSuggestProposalCreatedFromChain(tx.getHash(), true, from, to, info.getEventProposalId(), blockTime);
    //
    //         // send ntfc
    //         notificationService.sendProposalSuggestVoteFinsh(tx.getHash(), true, from, to, proposal.getProposalId(), info.getEventProposalId());
    //     }
    //
    // }


}
