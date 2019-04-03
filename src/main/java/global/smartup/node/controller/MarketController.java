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
import java.util.List;

@Api(description = "市场")
@RestController
@RequestMapping("/api/market")
public class MarketController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(MarketController.class);

    @Autowired
    private MarketService marketService;

    @Autowired
    private Validator validator;

    @ApiOperation(value = "创建市场", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：txHash, creatorAddress, name, description\n" +
                    "返回：是否成功")
    @RequestMapping("/create")
    public Object create(HttpServletRequest request, Market market) {
        try {
            String err = validator.validate(market, Market.Add.class);
            if (err != null) {
                return Wrapper.alert(err);
            }
            if (!Checker.isAddress(market.getCreatorAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketCreatorAddressFormatError));
            }
            if (marketService.isTxHashExist(market.getTxHash())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketTxHashRepeatError));
            }
            marketService.create(market);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "市场详情", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：marketAddress\n" +
                    "返回：obj = {\n" +
                    "　txHash, creatorAddress, marketAddress, name, description, \n" +
                    "　stage(creating=创建中, built=创建完成), createTime \n" +
                    "}")
    @RequestMapping("/one")
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
    @RequestMapping("/query/by/tx/hash")
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


    @ApiOperation(value = "更新市场address<暂用，后续由服务端处理>", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：txHash, marketAddress\n" +
                        "返回：是否成功")
    @RequestMapping("/update/market/address")
    public Object updateMarketAddress(HttpServletRequest request, String txHash, String marketAddress) {
        try {
            if (marketService.isTxHashExist(txHash)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketTxHashRepeatError));
            }
            if (!Checker.isAddress(marketAddress)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            marketService.updateBuilt(txHash, marketAddress);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "全部市场列表<暂用,后续会有各种排序>", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：无\n" +
                    "返回：obj = [ {见/api/market/one}, {}, ...]")
    @RequestMapping("/list")
    public Object list(HttpServletRequest request) {
        try {
            List list = marketService.queryAll();
            return Wrapper.success(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "创建者创建的市场列表", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：creatorAddress, pageNumb, pageSize\n" +
                    "返回：obj = {\n" +
                    "　list = [ {见/api/market/one}, {}, ...]\n" +
                    "}")
    @RequestMapping("/creator/created")
    public Object creatorCreated(HttpServletRequest request, String creatorAddress, Integer pageNumb, Integer pageSize) {
        try {
            if (!Checker.isAddress(creatorAddress)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.MarketCreatorAddressFormatError));
            }
            Pagination page = marketService.queryByCreator(creatorAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }


}
