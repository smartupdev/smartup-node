package global.smartup.node.service.block;

import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.ExchangeClient;
import global.smartup.node.eth.constract.event.CreateMarketEvent;
import global.smartup.node.eth.constract.func.CreateMarketFunc;
import global.smartup.node.po.Market;
import global.smartup.node.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 处理区块上的市场相关业务
 *   创建市场，挂单交易等
 */
@Service
public class BlockMarketService {

    private static final Logger log = LoggerFactory.getLogger(BlockFundService.class);

    @Autowired
    private EthClient ethClient;

    @Autowired
    private ExchangeClient exchangeClient;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private FundFlowService fundFlowService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private UserAccountService userAccountService;


    public void handleMarketCreate(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        if (transactionService.isTxHashNotExistOrHandled(tx.getHash())) {
            return;
        }
        CreateMarketFunc func = CreateMarketFunc.parse(tx);
        if (func == null) {
            return;
        }
        Market market = marketService.queryById(func.getMarketName());
        if (market == null) {
            return;
        }
        String userAddress = Keys.toChecksumAddress(tx.getFrom());
        boolean isSuccess = false;
        String marketAddress = null;
        if (ethClient.isTransactionSuccess(receipt)) {
            CreateMarketEvent event = CreateMarketEvent.parse(receipt);
            if (event == null) {
                return;
            }
            isSuccess = true;
            marketAddress = event.getCtAddress();
            // update account
            userAccountService.updateSutAndEth(userAddress, event.getSutRemain(), event.getEthRemain());
        }

        // update market
        marketService.updateCreateMarketFinish(market.getMarketId(), isSuccess, marketAddress, func.getDeposit(), func.getSupply(), func.getRate(), func.getLastRate());

        // update tr
        transactionService.modCreateMarketFinish(tx.getHash(), isSuccess, marketAddress, market.getMarketId(), market.getName(), func.getDeposit(), blockTime);

        // save fund record
        fundFlowService.addCreateMarket(tx.getHash(), isSuccess, userAddress, func.getDeposit(), func.getGasFee());

        // send notification
        notificationService.sendMarketCreateFinish(tx.getHash(), isSuccess, userAddress, market.getMarketId(), market.getName(), func.getDeposit());

    }

    public String createMarket(String userAddress, BigDecimal sut, String marketId, String symbol, BigDecimal ctCount,
                               BigDecimal ctPrice, BigDecimal ctRecyclePrice, Long closingTime, BigInteger gasLimit, BigInteger gasPrice,
                               String sign) {
        return exchangeClient.createMarket(userAddress, sut, marketId, symbol, ctCount, ctPrice, ctRecyclePrice, closingTime, gasLimit, gasPrice, sign);
    }

}
