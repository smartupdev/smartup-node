package global.smartup.node.controller;

import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.po.Market;
import global.smartup.node.po.Trade;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.TradeService;
import global.smartup.node.service.UserAccountService;
import global.smartup.node.util.Pagination;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.utils.Convert;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@Api(description = "交易")
@RestController
@RequestMapping("/api")
public class TradeController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private UserAccountService userAccountService;


    @ApiOperation(value = "交易详情", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：tradeId\n" +
                    "返回：obj = {\n" +
                    "　tradeId, userAddress, marketId, entrustVolume, entrustPrice, tradeVolume, tradePrice, fee, createTime, updateTime" +
                    "　type(firstStageBuy/buy/sell), state(nothing/cancel/half/halfCancel/done),\n" +
                    "　　childList = [\n" +
                    "　　　{\n" +
                    "　　　　tradeId, txHash, volume, price, createTime\n" +
                    "　　　　transaction = {\n" +
                    "　　　　　txHash, stage, type, userAddress, detail, createTime, blockTime\n" +
                    "　　　　}\n" +
                    "　　　}\n" +
                    "　　]\n" +
                    "}")
    @RequestMapping("/trade/one")
    public Object tradeOne(HttpServletRequest request, String tradeId) {
        try {
            Trade trade = tradeService.queryById(tradeId);
            return Wrapper.success(trade);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户交易列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：types, states, pageNumb, pageSize\n" +
                    "返回：obj = {\n" +
                    "　list = [ 见/api/trade/one ]\n" +
                    "}")
    @RequestMapping("/user/trade/list")
    public Object userTradeList(HttpServletRequest request, String[] types, String[] states, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            types = types != null ? types : new String[0];
            states = states != null ? states : new String[0];
            Pagination page = tradeService.queryByUserTrade(userAddress, Arrays.asList(types), Arrays.asList(states), pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "第一阶段买入CT", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketAddress, ctCount, gasLimit, gasPrice, timestamp, sign\n" +
                        "返回：见/api/trade/one")
    @RequestMapping("/user/first/stage/buy")
    public Object firstStageBuy(HttpServletRequest request, String marketAddress, BigDecimal ctCount,
                                BigInteger gasLimit, BigInteger gasPrice, String timestamp, String sign) {
        try {
            String userAddress = getLoginUserAddress(request);

            // check market
            Market market = marketService.queryByAddress(marketAddress);
            if (market == null) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketNotExist));
            }

            // check balance
            BigDecimal sut = BigDecimal.ZERO;
            BigDecimal gasFee = Convert.fromWei(new BigDecimal(gasPrice.multiply(gasLimit)), Convert.Unit.GWEI);
            Boolean hasEth = userAccountService.hasEnoughEth(userAddress, gasFee);
            Boolean hasSut = userAccountService.hasEnoughSut(userAddress, sut);
            if (hasEth == null || hasSut == null) {
                return Wrapper.alert(getLocaleMsg(LangHandle.NetWorkError));
            }
            if (!hasEth){
                return Wrapper.alert(getLocaleMsg(LangHandle.AccountEthNotEnough));
            }
            if (!hasSut){
                return Wrapper.alert(getLocaleMsg(LangHandle.AccountSutNotEnough));
            }

            Trade trade = tradeService.firstStageBuy(userAddress, market.getMarketId(), marketAddress, ctCount,
                market.getCtPrice(), gasLimit, gasPrice, timestamp, sign);
            if (trade == null) {
                return Wrapper.alert(getLocaleMsg(LangHandle.NetWorkError));
            }

            return Wrapper.success(trade);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    // @ApiOperation(value = "查询交易", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：txHash\n" +
    //                     "返回：obj = {\n" +
    //                     "　txHash, stage(pending, success, fail), userAddress, marketAddress, \n" +
    //                     "　type(buy, sell), sutOffer, sutAmount, ctAmount, createTime\n" +
    //                     "　user = { 见/api/user/current }" +
    //                     "}")
    // @RequestMapping("/user/trade/one")
    // public Object userTradeOne(HttpServletRequest request, String txHash) {
    //     try {
    //         Trade trade = tradeService.queryByTxHash(txHash);
    //         return Wrapper.success(trade);
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }
    //
    // @ApiOperation(value = "用户交易列表", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：type(buy, sell, 空为查询全部), pageNumb, pageSize\n" +
    //                     "返回：obj = { list = [ { 见/api/user/trade/one }, {}, .. ] }")
    // @RequestMapping("/user/trade/list")
    // public Object userTradeList(HttpServletRequest request, String type, Integer pageNumb, Integer pageSize) {
    //     try {
    //         String userAddress = getLoginUserAddress(request);
    //         Pagination<Trade> page = tradeService.queryByUser(userAddress, type, pageNumb, pageSize);
    //         return Wrapper.success(page);
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }
    //
    // @ApiOperation(value = "市场交易列表", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：marketAddress, type(buy, sell, 空为查询全部), asc(true createTime从小到大/false), pageNumb, pageSize\n" +
    //                     "返回：obj = { list = [ { 见/api/user/trade/one }, {}, .. ] }")
    // @RequestMapping("/market/trade/list")
    // public Object marketTradeList(HttpServletRequest request, String marketAddress, String type, Boolean asc, Integer pageNumb, Integer pageSize) {
    //     try {
    //         Pagination page = tradeService.queryByMarket(marketAddress, type, asc, pageNumb, pageSize);
    //         return Wrapper.success(page);
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }
    //
    // @ApiOperation(value = "保存用户交易", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：txHash, type(buy/sell), marketId, sut, ct\n" +
    //                     "返回：obj = { 见/api/user/trade/one }")
    // @RequestMapping("/user/trade/save")
    // public Object userTradeSave(HttpServletRequest request, String txHash, String type, String marketId, BigDecimal sut, BigDecimal ct) {
    //     try {
    //         String userAddress = getLoginUserAddress(request);
    //         if (!Checker.isTxHash(txHash)) {
    //             return Wrapper.alert(getLocaleMsg(LangHandle.TradeTxHashFormatError));
    //         }
    //         if (!PoConstant.Trade.Type.isRight(type)) {
    //             return Wrapper.alert(getLocaleMsg(LangHandle.TradeTypeError));
    //         }
    //         if (!marketService.isMarketIdExist(marketId)) {
    //             return Wrapper.alert(getLocaleMsg(LangHandle.MarketIdNotExist));
    //         }
    //         if (tradeService.isTxHashExist(txHash)) {
    //             return Wrapper.alert(getLocaleMsg(LangHandle.TradeTxHashRepeat));
    //         }
    //         Trade trade = tradeService.savePendingTrade(userAddress, txHash, type, marketId, sut, ct);
    //         return Wrapper.success(trade);
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }

}
