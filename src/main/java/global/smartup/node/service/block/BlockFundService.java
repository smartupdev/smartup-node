package global.smartup.node.service.block;

import global.smartup.node.constant.BuConstant;
import global.smartup.node.eth.EthClient;
import global.smartup.node.eth.constract.event.AdminWithdrawEvent;
import global.smartup.node.eth.constract.event.ChargeEvent;
import global.smartup.node.eth.constract.event.WithdrawEvent;
import global.smartup.node.eth.constract.func.AdminWithdrawFunc;
import global.smartup.node.eth.constract.func.ChargeSutFunc;
import global.smartup.node.eth.constract.func.WithdrawFunc;
import global.smartup.node.service.FundFlowService;
import global.smartup.node.service.NotificationService;
import global.smartup.node.service.TransactionService;
import global.smartup.node.service.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 处理区块上的资产充值、提现等
 */
@Service
public class BlockFundService {

    private static final Logger log = LoggerFactory.getLogger(BlockFundService.class);

    @Autowired
    private EthClient ethClient;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private FundFlowService fundFlowService;

    @Autowired
    private UserAccountService userAccountService;


    public void handleChargeSut(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        String userAddress = Keys.toChecksumAddress(tx.getFrom());
        ChargeSutFunc func = ChargeSutFunc.parse(tx);
        BigDecimal sut = func.getValue();
        boolean isSuccess = false;

        if (ethClient.isTransactionSuccess(receipt)) {
            isSuccess = true;
            // update balance
            ChargeEvent event = ChargeEvent.parse(receipt);
            userAccountService.updateSut(userAddress, event.getTotal());
        }

        // update tr
        transactionService.modChargeSutFinish(tx.getHash(), isSuccess, sut, blockTime);

        // save record
        fundFlowService.addChargeSut(tx.getHash(), isSuccess, userAddress, sut, BigDecimal.ZERO);

        // send notification
        notificationService.sendChargeSutFinish(isSuccess, userAddress, sut);
    }

    public void handleChargeEth(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        String userAddress = Keys.toChecksumAddress(tx.getFrom());
        BigDecimal eth = Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER).setScale(BuConstant.DefaultScale);
        boolean isSuccess = false;

        if (ethClient.isTransactionSuccess(receipt)) {
            isSuccess = true;
            // update balance
            ChargeEvent event = ChargeEvent.parse(receipt);
            userAccountService.updateEth(userAddress, event.getTotal());
        }

        // update tr
        transactionService.modChargeEthFinish(tx.getHash(), isSuccess, eth, blockTime);

        // save record
        fundFlowService.addChargeEth(tx.getHash(), isSuccess, userAddress, eth, BigDecimal.ZERO);

        // send notification
        notificationService.sendChargeEthFinish(isSuccess, userAddress, eth);
    }

    public void handleWithdrawSut(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        String userAddress = Keys.toChecksumAddress(tx.getFrom());
        WithdrawFunc func = WithdrawFunc.parse(tx);
        BigDecimal sut = func.getAmount();
        boolean isSuccess = false;
        if (ethClient.isTransactionSuccess(receipt)) {
            isSuccess = true;
            // update balance
            WithdrawEvent event = WithdrawEvent.parse(receipt);
            userAccountService.updateSut(userAddress, event.getReamain());
        }

        // update tr
        transactionService.modWithdrawSutFinish(tx.getHash(), isSuccess, sut, blockTime);

        // save record
        fundFlowService.addWithdrawSut(tx.getHash(), isSuccess, userAddress, sut, BigDecimal.ZERO);

        // send notification
        notificationService.sendWithdrawSutFinish(isSuccess, userAddress, sut);
    }

    public void handleWithdrawEth(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        String userAddress = Keys.toChecksumAddress(tx.getFrom());
        WithdrawFunc func = WithdrawFunc.parse(tx);
        BigDecimal eth = func.getAmount();
        boolean isSuccess = false;
        if (ethClient.isTransactionSuccess(receipt)) {
            isSuccess = true;
            // update balance
            WithdrawEvent event = WithdrawEvent.parse(receipt);
            userAccountService.updateEth(userAddress, event.getReamain());
        }

        // update tr
        transactionService.modWithdrawEthFinish(tx.getHash(), isSuccess, eth, blockTime);

        // save record
        fundFlowService.addWithdrawEth(tx.getHash(), isSuccess, userAddress, eth, BigDecimal.ZERO);

        // send notification
        notificationService.sendWithdrawEthFinish(isSuccess, userAddress, eth);
    }

    public void handleAdminWithdrawSut(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        String userAddress = Keys.toChecksumAddress(tx.getFrom());
        AdminWithdrawFunc func = AdminWithdrawFunc.parse(tx);
        BigDecimal sut = func.getAmount();
        boolean isSuccess = false;
        if (ethClient.isTransactionSuccess(receipt)) {
            isSuccess = true;
            // update balance
            AdminWithdrawEvent event = AdminWithdrawEvent.parse(receipt);
            userAccountService.updateSut(userAddress, event.getReamain());
        }

        // update tr
        transactionService.modWithdrawSutFinish(tx.getHash(), isSuccess, sut, blockTime);

        // save record
        fundFlowService.addWithdrawSut(tx.getHash(), isSuccess, userAddress, sut, BigDecimal.ZERO);

        // send notification
        notificationService.sendWithdrawSutFinish(isSuccess, userAddress, sut);
    }

    public void handleAdminWithdrawEth(Transaction tx, TransactionReceipt receipt, Date blockTime) {
        String userAddress = Keys.toChecksumAddress(tx.getFrom());
        AdminWithdrawFunc func = AdminWithdrawFunc.parse(tx);
        BigDecimal eth = func.getAmount();
        boolean isSuccess = false;
        if (ethClient.isTransactionSuccess(receipt)) {
            isSuccess = true;
            // update balance
            AdminWithdrawEvent event = AdminWithdrawEvent.parse(receipt);
            userAccountService.updateEth(userAddress, event.getReamain());
        }

        // update tr
        transactionService.modWithdrawEthFinish(tx.getHash(), isSuccess, eth, blockTime);

        // save record
        fundFlowService.addWithdrawEth(tx.getHash(), isSuccess, userAddress, eth, BigDecimal.ZERO);

        // send notification
        notificationService.sendWithdrawEthFinish(isSuccess, userAddress, eth);
    }

    public void callAdminWithdrawSut() {

    }

    public void callAdminWithdrawEth() {

    }

}
