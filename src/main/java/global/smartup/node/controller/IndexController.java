package global.smartup.node.controller;

import global.smartup.node.constant.LangHandle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class IndexController extends BaseController {

    @RequestMapping("/")
    public String root() {
        return "Smartup node is work";
    }

    @RequestMapping("/test/language")
    public String language(String code) {
        if (StringUtils.isBlank(code)) {
            code = LangHandle.TestLanguage;
        }
        return getLocaleMsg(code);
    }

}
