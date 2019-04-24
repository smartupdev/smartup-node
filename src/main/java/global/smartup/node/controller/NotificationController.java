package global.smartup.node.controller;

import global.smartup.node.service.NotificationService;
import global.smartup.node.util.Pagination;
import global.smartup.node.util.Wrapper;
import global.smartup.node.vo.UnreadNtfc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(description = "通知")
@RestController
@RequestMapping("/api/user/notification")
public class NotificationController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);


    @Autowired
    private NotificationService notificationService;


    @ApiOperation(value = "未读通知", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：无\n" +
                        "返回：obj = {\n" +
                        "　count = 未读通知数量\n" +
                        "　list = 最多前十条未读通知，见/user/notification/list\n" +
                        "}")
    @RequestMapping("/unread")
    public Object unread(HttpServletRequest request) {
        try {
            String userAddress = getLoginUserAddress(request);
            UnreadNtfc ntfc = notificationService.queryUnreadInCache(userAddress);
            return Wrapper.success(ntfc);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "通知列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：pageNumb, pageSize\n" +
                        "返回：obj = {\n" +
                        "　list = [\n" +
                        "　　{notificationId, userAddress, type, content(json字符串), isRead, createTime} , ... \n" +
                        "　]\n" +
                        "}\n" +
                        "type, content 说明：\n" +
                        "type = MarketCreateFinish, content = {title, txHash, isSuccess, marketId, userAddress,  marketAddress(isSuccess==false ? null:address)}\n" +
                        "type = TradeFinish, content = {title, txHash, isSuccess, userAddress, type(buy/sell), marketAddress, sut(isSuccess==false&&type==sell ? null:sut), ct}")
    @RequestMapping("/list")
    public Object list(HttpServletRequest request, Integer pageNumb, Integer pageSize) {
        try {
            Pagination page = notificationService.queryPage(getLoginUserAddress(request), pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "设为已读", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：notificationId\n" +
                        "返回：是否成功")
    @RequestMapping("/set/read")
    public Object setRead(HttpServletRequest request, Long notificationId) {
        try {
            notificationService.modRead(notificationId);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }


}
