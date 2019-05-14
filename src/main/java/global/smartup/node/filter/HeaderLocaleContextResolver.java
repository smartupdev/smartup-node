package global.smartup.node.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class HeaderLocaleContextResolver implements LocaleResolver {

    public static final String HeaderMark = "sn-language";

    public static final String DefaultLanguage = "en";

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String lang = request.getHeader(HeaderMark);
        if (StringUtils.isBlank(lang)) {
            return Locale.forLanguageTag(DefaultLanguage);
        } else {
            return Locale.forLanguageTag(lang);
        }

    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

    }
}


