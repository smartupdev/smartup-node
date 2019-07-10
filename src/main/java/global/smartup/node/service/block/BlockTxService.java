package global.smartup.node.service.block;

import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.EthClient;
import global.smartup.node.po.Transaction;
import global.smartup.node.service.TransactionService;
import global.smartup.node.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * 处理区块上的tx解析主类
 */
@Service
public class BlockTxService {

    @Autowired
    private EthClient ethClient;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private BlockFundService blockFundService;

    @Autowired
    private BlockMarketService blockMarketService;

    /**
     * 处理数据库中pending中的交易
     */
    public void handlePendingTransaction() {
        List<Transaction> transactionList = transactionService.queryPendingList();
        for (global.smartup.node.po.Transaction tr : transactionList) {
            org.web3j.protocol.core.methods.response.Transaction tx = ethClient.getTx(tr.getTxHash());
            BigInteger blockNumber;
            try {
                blockNumber = tx.getBlockNumber();
            } catch (Exception e) {
                continue;
            }
            TransactionReceipt receipt = ethClient.getTxReceipt(tx.getHash());
            EthBlock.Block block = ethClient.getBlockByNumber(blockNumber, false);
            if (tx == null || receipt == null || block == null) {
                // 有可能节点还没有同步到收据，放置到下一次处理
                continue;
            }
            Date blockTime = new Date(block.getTimestamp().longValue() * 1000);

            String userAddress = Keys.toChecksumAddress(tx.getFrom());
            if (userService.isNotExist(userAddress)) {
                userService.add(userAddress);
            }

            // 充值SUT
            if (tr.getType().startsWith(PoConstant.Transaction.Type.ChargeSut)) {
                blockFundService.handleChargeSut(tx, receipt, blockTime);
            }

            // 充值ETH
            if (tr.getType().startsWith(PoConstant.Transaction.Type.ChargeEth)) {
                blockFundService.handleChargeEth(tx, receipt, blockTime);
            }

            // 提取SUT
            if (tr.getType().startsWith(PoConstant.Transaction.Type.WithdrawSut)) {
                blockFundService.handleWithdrawSut(tx, receipt, blockTime);
            }

            // 提取ETH
            if (tr.getType().startsWith(PoConstant.Transaction.Type.WithdrawEth)) {
                blockFundService.handleWithdrawEth(tx, receipt, blockTime);
            }

            // 管理员提取SUT
            if (tr.getType().startsWith(PoConstant.Transaction.Type.AdminWithdrawSut)) {
                blockFundService.handleAdminWithdrawSut(tx, receipt, blockTime);
            }

            // 管理员提取ETH
            if (tr.getType().startsWith(PoConstant.Transaction.Type.AdminWithdrawEth)) {
                blockFundService.handleAdminWithdrawEth(tx, receipt, blockTime);
            }

            // 创建市场
            // if (tr.getType().startsWith(PoConstant.Transaction.Type.CreateMarket)) {
            //     blockMarketService.handleMarketCreate(tx, receipt, blockTime);
            // }

        }
    }

}
