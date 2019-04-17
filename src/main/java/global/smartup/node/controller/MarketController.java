package global.smartup.node.controller;

import global.smartup.node.compoment.Validator;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.po.Market;
import global.smartup.node.service.MarketService;
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

@Api(description = "市场")
@RestController
@RequestMapping("/api")
public class MarketController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(MarketController.class);

    @Autowired
    private MarketService marketService;

    @Autowired
    private Validator validator;

    @ApiOperation(value = "保存市场", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：name, description\n" +
                    "返回：是否成功")
    @RequestMapping("/user/market/save")
    public Object save(HttpServletRequest request, Market market) {
        try {
            String err = validator.validate(market, Market.Add.class);
            if (err != null) {
                return Wrapper.alert(err);
            }
            String userAddress = getLoginUserAddress(request);
            boolean isRepeat = marketService.isNameRepeat(userAddress, market.getName());
            if (isRepeat) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketNameRepeat));
            }
            market.setCreatorAddress(userAddress);
            marketService.save(market);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "正在创建的市场", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：无 \n" +
                        "返回：obj = { 见/api/market/one }")
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

    @ApiOperation(value = "判断市场名字正确", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketName\n" +
                        "返回：是否正确")
    @RequestMapping("/user/market/is/name/right")
    public Object isNameRight(HttpServletRequest request, String marketName) {
        try {
            Market market = new Market();
            market.setName(marketName);
            String err = validator.validate(market, Market.CheckName.class);
            if (err != null) {
                return Wrapper.alert(err);
            }
            boolean isRepeat = marketService.isNameRepeat(getLoginUserAddress(request), marketName);
            if (isRepeat) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketNameRepeat));
            }
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "市场详情", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：marketAddress\n" +
                    "返回：obj = {\n" +
                    "　marketId, txHash, creatorAddress, marketAddress, name, description, \n" +
                    "　stage(creating=创建中, built=创建完成, fail=创建失败, close=已关闭), createTime \n" +
                    "　data = { latelyChange, last, latelyVolume, amount, ctAmount, ctTopAmount, count } \n" +
                    "}")
    @RequestMapping("/market/one")
    public Object one(HttpServletRequest request, String marketAddress) {
        try {
            if (!Checker.isAddress(marketAddress)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketCreatorAddressFormatError));
            }
            return Wrapper.success(marketService.queryByAddress(marketAddress));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "市场详情", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：txHash\n" +
                    "返回：见/api/market/one")
    @RequestMapping("/market/query/by/tx/hash")
    public Object queryByTxHash(HttpServletRequest request, String txHash) {
        try {
            if (!Checker.isTxHash(txHash)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketTxHashFormatError));
            }
            return Wrapper.success(marketService.queryByTxHash(txHash));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "全部市场列表", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：orderBy(lately_change, last, lately_volume, amount, count), asc(true从小到大/false) \n" +
                    "　pageNumb, pageSize\n" +
                    "返回：obj = { list = [ {见/api/market/one}, {}, ...] }")
    @RequestMapping("/market/list")
    public Object list(HttpServletRequest request, String orderBy, Boolean asc, Integer pageNumb, Integer pageSize) {
        try {
            Pagination page = marketService.queryPage(orderBy, asc, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户创建的市场", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：creatorAddress, pageNumb, pageSize\n" +
                    "返回：obj = {\n" +
                    "　list = [ {见/api/market/one}, {}, ...]\n" +
                    "}")
    @RequestMapping("/user/market/created")
    public Object creatorCreated(HttpServletRequest request, Integer pageNumb, Integer pageSize) {
        try {
            String creatorAddress = getLoginUserAddress(request);
            Pagination page = marketService.queryByCreator(creatorAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }


}
