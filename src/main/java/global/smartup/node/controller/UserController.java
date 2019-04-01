package global.smartup.node.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @RequestMapping("/update")
    public Object update(HttpServletRequest request) {
        try {

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "update";
    }



}
