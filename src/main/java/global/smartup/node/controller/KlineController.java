package global.smartup.node.controller;

import global.smartup.node.constant.LangHandle;
import global.smartup.node.po.KlineNode;
import global.smartup.node.service.KlineNodeService;
import global.smartup.node.service.MarketService;
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
import java.util.List;

@Api(description = "k线")
@RestController
@RequestMapping("/api/kline")
public class KlineController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(KlineController.class);

    @Autowired
    private KlineNodeService klineNodeService;

    @Autowired
    private MarketService marketService;

    @ApiOperation(value = "查询k线数据", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：marketAddress, segment(1hour, 1day, 1week), start, end\n" +
                        "　当segment=1hour, start/end='yyyy_MM_dd_HH' \n" +
                        "　当segment=1day/1week, start/end='yyyy_MM_dd' \n" +
                        "返回：obj = [\n" +
                        "　{marketAddress, timeId, segment, high, low, start, end, amount, count, time} , ... \n" +
                        "]\n" +
                        "")
    @RequestMapping("/data")
    public Object data(HttpServletRequest request, String marketAddress, String segment, String start, String end) {
        try {
            if (!Checker.isNodeSegment(segment)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.KlineSegmentFormatError));
            }
            if (!Checker.isNodeSegmentTimeId(segment, start) || !Checker.isNodeSegmentTimeId(segment, end)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.KlineTimeIdFormatError));
            }
            // get data
            List<KlineNode> list = klineNodeService.queryCacheNodes(marketAddress, segment, start, end);
            return Wrapper.success(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

}
