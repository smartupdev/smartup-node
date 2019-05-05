package global.smartup.node.controller;


import global.smartup.node.service.CTAccountService;
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

@Api(description = "账户")
@RestController
@RequestMapping("/api")
public class AccountController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private CTAccountService ctAccountService;

    @ApiOperation(value = "CT账户和市场信息", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：pageNumb, pageSize\n" +
                        "返回：obj = { list = [ {marketId, marketAddress, marketName, marketCover, marketPhoto, latelyChange, userAddress, ctAmount, lastUpdateTime}, ... ] }")
    @RequestMapping("/user/ct/account/in/market")
    public Object ctAccountInMarket(HttpServletRequest request, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            Pagination page = ctAccountService.queryCTAccountsWithMarket(userAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

}
