package global.smartup.node.controller;

import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.service.TransactionService;
import global.smartup.node.util.Checker;
import global.smartup.node.util.Pagination;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(description = "区块交易")
@RestController
@RequestMapping("/api")
public class TransactionController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @ApiOperation(value = "用户区块交易列表", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：type(ChargeSut/ChargeEth/.../可空), pageNumb, pageSize\n" +
                    "返回：obj = {\n" +
                    "　txHash, stage(pending/success/fail), type, userAddress, detail, createTime, blockTime\n" +
                    "　type = ChargeSut, detail = {sut}\n" +
                    "　type = ChargeEth, detail = {eth}\n" +
                    "　type = WithdrawSut, detail = {sut}\n" +
                    "　type = WithdrawEth, detail = {eth}\n" +
                    "　type = CreateMarket, detail = {...}\n" +
                    "　type = BuyCT, detail = {...}\n" +
                    "　type = SellCT, detail = {...}\n" +
                    "}")
    @RequestMapping("/user/transaction/list")
    public Object transactionList(HttpServletRequest request, Integer pageNumb, Integer pageSize) {
        try {
            Pagination page = transactionService.queryPage(getLoginUserAddress(request), pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "上传区块交易哈希", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：txHash, type(见/user/transaction/list)\n" +
                        "返回：是否成功")
    @RequestMapping("/user/transaction/upload/tx/hash")
    public Object uploadTxHash(HttpServletRequest request, String txHash, String type) {
        try {
            if (!PoConstant.Transaction.Type.isRightType(type)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.TransactionTypeError));
            }
            if (!Checker.isTxHash(txHash)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.TransactionTxHashError));
            }
            transactionService.addPending(txHash, getLoginUserAddress(request), type);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }


}
