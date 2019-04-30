package global.smartup.node.controller;

import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.po.Trade;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.TradeService;
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
import java.math.BigDecimal;

@Api(description = "交易")
@RestController
@RequestMapping("/api")
public class TradeController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;

    @Autowired
    private MarketService marketService;

    @ApiOperation(value = "查询交易", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：txHash\n" +
                        "返回：obj = {\n" +
                        "　txHash, stage(pending, success, fail), userAddress, marketAddress, \n" +
                        "　type(buy, sell), sutOffer, sutAmount, ctAmount, createTime\n" +
                        "　user = { 见/api/user/current }" +
                        "}")
    @RequestMapping("/user/trade/one")
    public Object userTradeOne(HttpServletRequest request, String txHash) {
        try {
            Trade trade = tradeService.queryByTxHash(txHash);
            return Wrapper.success(trade);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户交易列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：type(buy, sell, 空为查询全部), pageNumb, pageSize\n" +
                        "返回：obj = { list = [ { 见/api/user/trade/one }, {}, .. ] }")
    @RequestMapping("/user/trade/list")
    public Object userTradeList(HttpServletRequest request, String type, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            Pagination<Trade> page = tradeService.queryByUser(userAddress, type, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "市场交易列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketAddress, type(buy, sell, 空为查询全部), asc(true createTime从小到大/false), pageNumb, pageSize\n" +
                        "返回：obj = { list = [ { 见/api/user/trade/one }, {}, .. ] }")
    @RequestMapping("/market/trade/list")
    public Object marketTradeList(HttpServletRequest request, String marketAddress, String type, Boolean asc, Integer pageNumb, Integer pageSize) {
        try {
            Pagination page = tradeService.queryByMarket(marketAddress, type, asc, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "保存用户交易", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：txHash, type(buy/sell), marketId, sut, ct\n" +
                        "返回：是否成功")
    @RequestMapping("/user/trade/save")
    public Object userTradeSave(HttpServletRequest request, String txHash, String type, String marketId, BigDecimal sut, BigDecimal ct) {
        try {
            String userAddress = getLoginUserAddress(request);
            if (!Checker.isTxHash(txHash)) {
                Wrapper.alert(getLocaleMsg(LangHandle.TradeTxHashFormatError));
            }
            if (!PoConstant.Trade.Type.isRight(type)) {
                Wrapper.alert(getLocaleMsg(LangHandle.TradeTypeError));
            }
            if (!marketService.isMarketIdExist(marketId)) {
                Wrapper.alert(getLocaleMsg(LangHandle.MarketIdNotExist));
            }
            tradeService.savePendingTrade(userAddress, txHash, type, marketId, sut, ct);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

}
