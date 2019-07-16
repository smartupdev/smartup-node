package global.smartup.node.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class BaseService {

    @Autowired
    MessageSource messageSource;

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

}
