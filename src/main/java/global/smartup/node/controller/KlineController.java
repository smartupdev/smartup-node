package global.smartup.node.controller;

import global.smartup.node.po.KlineNode;
import global.smartup.node.service.KlineNodeService;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(description = "k线")
@Controller
@RequestMapping("/api/kline")
public class KlineController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(KlineController.class);

    @Autowired
    private KlineNodeService klineNodeService;

    @ApiOperation(value = "查询k线数据", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketAddress, segment, start, end, range\n" +
                        "返回： ")
    @RequestMapping("/data")
    public Object data(HttpServletRequest request, String marketAddress, String segment, String start, String end, Integer range) {
        try {
            List<KlineNode> list = klineNodeService.queryRangeNodes(marketAddress, segment, start, end);
            return Wrapper.success(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

}
