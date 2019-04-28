package global.smartup.node.controller;

import global.smartup.node.constant.LangHandle;
import global.smartup.node.service.CollectService;
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

@Api(description = "收藏")
@RestController
@RequestMapping("/api/user/collect")
public class CollectController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(CollectController.class);

    @Autowired
    private CollectService collectService;


    @ApiOperation(value = "添加收藏", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：type(market/post/reply), \n" +
                        "　objectMark:\n" +
                        "　　type=market objectMark=marketId\n" +
                        "　　type=post objectMark=postId\n" +
                        "　　type=reply objectMark=replyId\n" +
                        "返回：是否成功")
    @RequestMapping("/add")
    public Object add(HttpServletRequest request, String type, String objectMark) {
        try {
            if (!collectService.isType(type)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.CollectTypeError));
            }
            if (!collectService.isObjectExist(type, objectMark)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.CollectObjectNotExist));
            }
            collectService.add(getLoginUserAddress(request), type, objectMark);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "取消收藏", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：见 /api/user/collect/add\n" +
                        "返回：是否成功")
    @RequestMapping("/del")
    public Object del(HttpServletRequest request, String type, String objectMark) {
        try {
            collectService.del(getLoginUserAddress(request), type, objectMark);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @Deprecated
    @ApiOperation(value = "收藏列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：type, pageNumb, pageSize\n" +
                        "返回：obj = {\n" +
                        "　list = [\n" +
                        "　　{type=market, 见/api/market/one}\n" +
                        "　　{type=post, 见/api/post/one}\n" +
                        "　]\n" +
                        "}")
    @RequestMapping("/list")
    public Object list(HttpServletRequest request, String type, Integer pageNumb, Integer pageSize) {
        try {
            Pagination page = collectService.queryTypePage(getLoginUserAddress(request), type, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

}
