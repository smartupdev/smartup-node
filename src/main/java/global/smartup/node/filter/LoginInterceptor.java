package global.smartup.node.filter;

import global.smartup.node.constant.RedisKey;
import global.smartup.node.controller.BaseController;
import global.smartup.node.util.Wrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        // Locale locale = LocaleContextHolder.getLocale();

        if (uri.startsWith("/api/user")) {
            // if (!uri.startsWith("/api/user/login") && !uri.startsWith("/api/user/auth")) { }
            String token = request.getHeader(BaseController.TokenName);
            if (StringUtils.isBlank(token)) {
                notLogin(response);
                return false;
            }
            Object address  = redisTemplate.opsForValue().get(RedisKey.UserTokenPrefix + token);
            if (address == null) {
                notLogin(response);
                return false;
            }
            request.setAttribute(BaseController.LoginUserAddressRequestMark, address.toString());
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    private void notLogin(HttpServletResponse response) {
        try {
            Wrapper wrapper = Wrapper.notLogin();
            response.setHeader("Content-Type", "application/json;charset=UTF-8");
            response.getWriter().print(wrapper.toJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
