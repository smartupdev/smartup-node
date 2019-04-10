package global.smartup.node.controller;

import global.smartup.node.po.Trade;
import global.smartup.node.service.TradeService;
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

@Api(description = "交易")
@RestController
@RequestMapping("/api")
public class TradeController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;


    @ApiOperation(value = "查询交易", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：txHash\n" +
                        "返回：obj = {\n" +
                        "　txHash, stage(pending, success, fail), userAddress, marketAddress, \n" +
                        "　type(buy, sell), sutOffer, sutAmount, ctAmount, createTime\n" +
                        "}")
    @RequestMapping("/user/trade/one")
    public Object userTradeOne(HttpServletRequest request, String txHash) {
        try {
            Trade trade = tradeService.query(txHash);
            return Wrapper.success(trade);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "查询交易列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：type, pageNumb, pageSize\n" +
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


}
