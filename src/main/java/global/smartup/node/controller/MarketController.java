package global.smartup.node.controller;

import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.po.Market;
import global.smartup.node.service.GlobalService;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.UserAccountService;
import global.smartup.node.util.Checker;
import global.smartup.node.util.Common;
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
import java.util.HashMap;
import java.util.Map;

@Api(description = "市场")
@RestController
@RequestMapping("/api")
public class MarketController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(MarketController.class);

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private MarketService marketService;

    @Autowired
    private GlobalService globalService;

    @Autowired
    private UserAccountService userAccountService;


    @ApiOperation(value = "检查创建市场信息", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：name, description, detail, photo, cover\n" +
                        "返回： \n" +
                        "　如果参数正确 code = 0, obj = null \n" +
                        "　如果参数错误 code = 4, obj = {\n" +
                        "　　'name': 'xxxxxx', \n" +
                        "　　... \n" +
                        "　}")
    @RequestMapping("/market/create/check/info")
    public Object checkInfo(HttpServletRequest request, String name, String description, String detail, String photo, String cover) {
        try {
            Map<String, String> err = marketService.checkMarketInfo(null, name, description, detail, photo, cover);
            if (err.size() != 0) {
                return Wrapper.paramError(err);
            } else {
                return Wrapper.success();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "检查创建市场设置", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：symbol, ctCount, ctPrice, ctRecyclePrice, closingTime\n" +
                    "返回：见/api/market/create/check/info")
    @RequestMapping("/market/create/check/setting")
    public Object checkSetting(HttpServletRequest request, String symbol, BigDecimal ctCount, BigDecimal ctPrice, BigDecimal ctRecyclePrice, Long closingTime) {
        try {
            Map<String, String> err = marketService.checkMarketSetting(null, symbol, ctCount, ctPrice, ctRecyclePrice, closingTime);
            if (err.size() != 0) {
                return Wrapper.paramError(err);
            } else {
                return Wrapper.success();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "生成一个市场id", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：无\n" +
                        "返回：obj = {marketId}")
    @RequestMapping("/market/create/generate/id")
    public Object generateId(HttpServletRequest request) {
        try {
            String id = idGenerator.getHexStringId();
            return Wrapper.success(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "创建/修改 市场", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketId, name, symbol, description, detail, photo, cover, ctCount, ctPrice, ctRecyclePrice, \n" +
                        "closingTime(过期时间，未来的一个时间的时间戳，以秒为单位), gasLimit, gasPrice, sign\n" +
                        "返回：\n" +
                        "　如果参数错误，code = 4, 见/api/market/create/check/info\n" +
                        "　如果创建失败，code = 2, msg = 'xxxx' \n" +
                        "　如果创建成功, code = 0, obj = { 见/api/market/one }\n" +
                        "　　market.status = 'creating' 发送交易失败，可以通过marketId，获取市场信息重新发送交易\n" +
                        "　　market.status = 'locked' 发送交易成功"
    )
    @RequestMapping("/user/market/create")
    public Object create(HttpServletRequest request, String marketId, String name, String symbol, String description,
                         String detail, String photo, String cover,
                         BigDecimal ctCount, BigDecimal ctPrice, BigDecimal ctRecyclePrice, Long closingTime,
                         BigInteger gasLimit, BigInteger gasPrice, String sign) {
        try {
            String userAddress = getLoginUserAddress(request);

            // check has creating market
            if (!Common.isRightMarketId(marketId)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketIdFormatError));
            }
            Market current = marketService.queryCurrentCreating(getLoginUserAddress(request));
            if (current != null) {
                if (PoConstant.Market.Status.Locked.equals(current.getStatus())) {
                    return Wrapper.alert(getLocaleMsg(LangHandle.MarketIsCreating));
                }
                if (!current.getMarketId().equals(marketId)) {
                    return Wrapper.alert(getLocaleMsg(LangHandle.MarketIsCreating));
                }
            } else {
                if (marketService.isIdRepeat(marketId)) {
                    return Wrapper.alert(getLocaleMsg(LangHandle.MarketIdRepeat));
                }
            }

            // check param
            Map<String, String> err = new HashMap<>();
            Map<String, String> err1 = marketService.checkMarketInfo(userAddress, name, description, detail, photo, cover);
            Map<String, String> err2 = marketService.checkMarketSetting(userAddress, symbol, ctCount, ctPrice, ctRecyclePrice, closingTime);
            if (err1.size() > 0 || err2.size() > 0) {
                err.putAll(err1);
                err.putAll(err2);
                return Wrapper.paramError(err);
            }

            // check gas price
            if (Checker.isGasPriceRight(gasPrice)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.TransactionGasPriceError));
            }

            // check balance
            BigDecimal gasFee = Convert.fromWei(new BigDecimal(gasPrice.multiply(gasLimit)), Convert.Unit.GWEI);
            Boolean hasEth = userAccountService.hasEnoughEth(userAddress, gasFee);
            Boolean hasSut = userAccountService.hasEnoughSut(userAddress, BuConstant.MarketInitSut);
            if (hasEth == null || hasSut == null) {
                return Wrapper.alert(getLocaleMsg(LangHandle.NetWorkError));
            }
            if (!hasEth){
                return Wrapper.alert(getLocaleMsg(LangHandle.AccountEthNotEnough));
            }
            if (!hasSut){
                return Wrapper.alert(getLocaleMsg(LangHandle.AccountSutNotEnough));
            }

            // create market
            Market market = marketService.saveAndPay(marketId, userAddress, name, symbol, description, photo, cover,
                ctCount, ctPrice, ctRecyclePrice, closingTime, gasLimit, gasPrice, sign);
            return Wrapper.success(market);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "正在创建的市场", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：无 \n" +
                        "返回：obj = { 见/api/market/one }\n" +
                        "　查询 market.status = (creating/locked/fail) 的市场")
    @RequestMapping("/user/market/creating")
    public Object creating(HttpServletRequest request) {
        try {
            Market market = marketService.queryCurrentCreating(getLoginUserAddress(request));
            return Wrapper.success(market);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    // @ApiOperation(value = "支付后锁定市场", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：marketId, txHash\n" +
    //                     "返回：是否成功")
    // @RequestMapping("/user/market/lock")
    // public Object lock(HttpServletRequest request, String marketId, String txHash) {
    //     try {
    //         marketService.updateLock(marketId, txHash);
    //         return Wrapper.success();
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }

    // @ApiOperation(value = "判断市场名字正确", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：marketName\n" +
    //                     "返回：是否正确")
    // @RequestMapping("/user/market/is/name/right")
    // public Object isNameRight(HttpServletRequest request, String marketName) {
    //     try {
    //         Market market = new Market();
    //         market.setName(marketName);
    //         String err = validator.validate(market, Market.CheckName.class);
    //         if (err != null) {
    //             return Wrapper.alert(err);
    //         }
    //         boolean isRepeat = marketService.isNameRepeat(getLoginUserAddress(request), marketName);
    //         if (isRepeat) {
    //             return Wrapper.alert(getLocaleMsg(LangHandle.MarketNameRepeat));
    //         }
    //         return Wrapper.success();
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }

    @ApiOperation(value = "市场详情", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：marketId\n" +
                    "返回：obj = {\n" +
                    "　marketId, txHash, creatorAddress, marketAddress, name, description, detail, createTime\n" +
                    "　ctCount, ctPrice, ctRecyclePrice\n" +
                    "　status(creating=编辑, locked=锁定, open=开放, close=关闭, fail=失败)\n" +
                    "　data = { latelyChange, last, latelyVolume, amount, ctAmount, ctTopAmount, count, postCount, userCount } \n" +
                    "　creator = { 见/api/user/current } \n" +
                    "}")
    @RequestMapping("/market/one")
    public Object marketOneById(HttpServletRequest request, String marketId) {
        try {
            Market market = marketService.queryWithDataById(marketId);
            marketService.queryUserCollect(getLoginUserAddress(request), market);
            return Wrapper.success(market);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    // @ApiOperation(value = "市场详情", httpMethod = "POST", response = Wrapper.class,
    //         notes = "参数：txHash\n" +
    //                 "返回：见/api/market/one")
    // @RequestMapping("/market/query/by/tx/hash")
    // public Object queryByTxHash(HttpServletRequest request, String txHash) {
    //     try {
    //         if (!Checker.isTxHash(txHash)) {
    //             return Wrapper.alert(getLocaleMsg(LangHandle.MarketTxHashFormatError));
    //         }
    //         Market market = marketService.queryByTxHash(txHash);
    //         marketService.queryUserCollect(getLoginUserAddress(request), market);
    //         return Wrapper.success(market);
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }

    @ApiOperation(value = "全部市场列表", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：orderBy(lately_change, last, lately_volume, amount, count), asc(true从小到大/false) \n" +
                    "　pageNumb, pageSize\n" +
                    "返回：obj = { list = [ {见/api/market/one}, {}, ...] }")
    @RequestMapping("/market/list")
    public Object marketList(HttpServletRequest request, String orderBy, Boolean asc, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            Pagination page = marketService.queryPage(userAddress, orderBy, asc, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "搜索市场", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：name, orderBy(lately_change, last, lately_volume, amount, count), asc(true从小到大/false) \n" +
                    "返回：obj = { list = [ {见/api/market/one}, {}, ...] }")
    @RequestMapping("/market/search")
    public Object marketSearch(HttpServletRequest request, String name, String orderBy, Boolean asc, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            Pagination page = marketService.querySearchPage(userAddress, name, orderBy, asc, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    // @ApiOperation(value = "市场top", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：type(hottest, newest, populous, richest), limit(20~100) \n" +
    //                     "返回：obj = { list = [ {见/api/market/one}, {}, ...] }")
    // @RequestMapping("/market/top")
    // public Object marketTop(HttpServletRequest request, String type, Integer limit) {
    //     try {
    //         List ret = marketService.queryTop(getLoginUserAddress(request), type, limit);
    //         return Wrapper.success(ret);
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }

    @ApiOperation(value = "全部市场数据", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：无\n" +
                        "返回：sutAmount, marketCount, latelyPostCount")
    @RequestMapping("/market/global/data")
    public Object marketGlobalData(HttpServletRequest request) {
        try {
            return Wrapper.success(globalService.queryGlobalData());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    // @ApiOperation(value = "市场top用户", httpMethod = "POST", response = Wrapper.class,
    //             notes = "参数：marketId\n" +
    //                     "返回：obj = {\n" +
    //                     " topCtList = [ { 见/api/user/current } ] \n" +
    //                     " topPostList = [ ... ]\n" +
    //                     " topLikedList = [ ... ]\n" +
    //                     "}")
    // @RequestMapping("/market/user/top")
    // public Object marketUserTop(HttpServletRequest request, String markId) {
    //     try {
    //         HashMap<String, Object> ret = new HashMap<>();
    //         ret.put("topCtList", marketService.queryTopCTUser(markId));
    //         ret.put("topPostList", marketService.queryTopPostUser(markId));
    //         ret.put("topLikedList", marketService.queryTopLikedUser(markId));
    //         return Wrapper.success(ret);
    //     } catch (Exception e) {
    //         log.error(e.getMessage(), e);
    //         return Wrapper.sysError();
    //     }
    // }

}
