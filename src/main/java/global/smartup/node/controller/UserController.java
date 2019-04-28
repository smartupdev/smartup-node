package global.smartup.node.controller;


import global.smartup.node.compoment.Validator;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.RedisKey;
import global.smartup.node.eth.EthClient;
import global.smartup.node.po.User;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.TransactionService;
import global.smartup.node.service.UserService;
import global.smartup.node.util.Checker;
import global.smartup.node.util.Pagination;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.Keys;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(description = "用户")
@RestController
@RequestMapping("/api")
public class UserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private EthClient ethClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Validator validator;

    @Autowired
    private MarketService marketService;


    @ApiOperation(value = "登录", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：address\n" +
                        "返回：code")
    @RequestMapping("/login")
    public Object login(HttpServletRequest request, String address) {
        try {
            if (!Checker.isAddress(address)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            User u;
            if (!userService.isExist(address)) {
                u = userService.add(address);
            } else {
                u = userService.query(address);
            }
            return Wrapper.success(u.getCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "认证", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：address, signature(使用code签名获得)\n" +
                        "返回：obj = {\n" +
                        "　token,\n" +
                        "　user = { 见/api/user/current }\n" +
                        "}")
    @RequestMapping("/auth")
    public Object auth(HttpServletRequest request, String address, String signature) {
        try {
            if (!Checker.isAddress(address)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            address = Keys.toChecksumAddress(address);
            if (!userService.isExist(address)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.UserAddressNotExist));
            }
            User user = userService.query(address);
            boolean isSignOK = ethClient.recoverSignature(address, user.getCode(), signature);
            if (isSignOK) {
                // update code
                userService.updateCode(address);

                // cache token
                String token = userService.generateCode();
                redisTemplate.opsForValue().set(RedisKey.UserTokenPrefix + token, address, TokenExpire , TimeUnit.MILLISECONDS);

                Map<String, Object> ret = new HashMap<>();
                ret.put("user", user);
                ret.put("token", token);
                return Wrapper.success(ret);
            } else {
                return Wrapper.alert(getLocaleMsg(LangHandle.UserSignatureError));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "当前用户", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：无\n" +
                        "返回：obj = {\n" +
                        "　address, name, avatarIpfsHash, createTime\n" +
                        "}")
    @RequestMapping("/user/current")
    public Object current(HttpServletRequest request) {
        try {
            String address = getLoginUserAddress(request);
            User user = userService.query(address);
            user.setCode(null);
            return Wrapper.success(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "更新用户", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：name(可以空), avatarIpfsHash(可以空)\n" +
                        "返回：是否成功")
    @RequestMapping("/user/update")
    public Object update(HttpServletRequest request, User user) {
        try {
            String address = getLoginUserAddress(request);
            user.setUserAddress(address);
            if (!Checker.isAddress(user.getUserAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            String err = validator.validate(user, User.Add.class);
            if (err != null) {
                return Wrapper.alert(err);
            }
            if (!userService.isExist(user.getUserAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.UserAddressNotExist));
            }
            userService.update(user);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户交易列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：pageNumb, pageSize\n" +
                        "返回：obj = {\n" +
                        "　txHash, stage(pending/success/fail), type, userAddress, marketId, marketAddress, detail, createTime, blockTime\n" +
                        "　type = CreateMarket, detail = {sut}\n" +
                        "　type = BuyCT, detail = {sut, ct}\n" +
                        "　type = SellCT, detail = {sut(stage==fail ? null:sut), ct}\n" +
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

    @ApiOperation(value = "用户创建的市场", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：pageNumb, pageSize\n" +
                    "返回：obj = {\n" +
                    "　list = [ {见/api/market/one}, {}, ...]\n" +
                    "}")
    @RequestMapping("/user/market/created")
    public Object userCreatedMarket(HttpServletRequest request, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            Pagination page = marketService.queryByCreator(userAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "用户交易的市场", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：pageNumb, pageSize\n" +
                    "返回：obj = {\n" +
                    "　list = [ {见/api/market/one}, {}, ...]\n" +
                    "}")
    @RequestMapping("/user/market/traded")
    public Object userTradedMarket(HttpServletRequest request, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            Pagination page = marketService.queryUserTraded(userAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }



}
