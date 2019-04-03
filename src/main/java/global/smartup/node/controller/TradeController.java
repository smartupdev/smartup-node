package global.smartup.node.controller;

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
@RequestMapping("/api/trade")
public class TradeController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);


    @ApiOperation(value = "买入CT", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：userAddress, txHash\n" +
                        "返回：是否成功")
    @RequestMapping("/buy/ct")
    public Object buyCT(HttpServletRequest request) {
        try {

            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }


    @ApiOperation(value = "卖入CT", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：userAddress, txHash\n" +
                        "返回：是否成功")
    @RequestMapping("/sell/ct")
    public Object sellCT(HttpServletRequest request) {
        try {

            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }



}
