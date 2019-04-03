package global.smartup.node.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class IndexController {

    @RequestMapping("/")
    public String root() {
        return "Smartup node is work";
    }



}
