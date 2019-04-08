package global.smartup.node.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseController {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    public static final String LoginUserAddressRequestMark = "login_user_address";

    public static final Integer TokenExpire = 7 * 24 * 60 * 60 * 1000;

    public static final String TokenName = "token";

    @Autowired
    MessageSource messageSource;

    @InitBinder
    public void initBinder(WebDataBinder binder, WebRequest request) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    public String getLocaleMsg(String code) {
        return getLocaleMsg(code, null);
    }

    public String getLocaleMsg(String code, String... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLoginUserAddress(HttpServletRequest request) {
        Object address = request.getAttribute(LoginUserAddressRequestMark);
        if (address == null) {
            return null;
        }
        return address.toString();
    }

}
