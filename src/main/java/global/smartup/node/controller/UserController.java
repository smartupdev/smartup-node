package global.smartup.node.controller;


import global.smartup.node.compoment.Validator;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.po.User;
import global.smartup.node.service.UserService;
import global.smartup.node.util.Checker;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(description = "用户")
@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Validator validator;

    @ApiOperation(value = "添加用户", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：address, name(可以空), avatarIpfsHash(可以空)\n" +
                        "返回：是否成功")
    @RequestMapping("/add")
    public Object add(HttpServletRequest request, User user) {
        try {
            if (!Checker.isAddress(user.getUserAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            String err = validator.validate(user, User.Add.class);
            if (err != null) {
                return Wrapper.alert(err);
            }
            if (userService.isExist(user.getUserAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.UserAddressAlreadyExist));
            }
            userService.add(user);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "查询用户", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：address\n" +
                        "返回：obj = {\n" +
                        "　address, name, avatarIpfsHash, createTime\n" +
                        "}")
    @RequestMapping("/query")
    public Object query(HttpServletRequest request, String address) {
        try {
            if (!Checker.isAddress(address)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            User user = userService.query(address);
            return Wrapper.success(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "地址是否存在", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：address\n" +
                        "返回：是否存在")
    @RequestMapping("/exist/address")
    public Object existAddress(HttpServletRequest request, String address) {
        try {
            if (!Checker.isAddress(address)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            return Wrapper.success(userService.isExist(address));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "更新用户", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：address, name(可以空), avatarIpfsHash(可以空)\n" +
                        "返回：是否成功")
    @RequestMapping("/update")
    public Object update(HttpServletRequest request, User user) {
        try {
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



}
