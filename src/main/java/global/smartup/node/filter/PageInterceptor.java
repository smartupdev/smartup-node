package global.smartup.node.filter;

import global.smartup.node.util.Pagination;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 过滤service中需要分页的函数，对pageNumb、pageSize为空或者异常的情况的预处理。
 */
@Aspect
@Component
public class PageInterceptor {


    @Around("execution (* global.smartup.node.service.*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
        try {
            String classType = joinPoint.getTarget().getClass().getName();
            Class<?> clazz = Class.forName(classType);
            String clazzName = clazz.getName();
            String methodName = joinPoint.getSignature().getName();
            List<String> paramNames = getFieldsName(clazz, clazzName, methodName, parameterTypes);
            for (int i = 0; i < paramNames.size(); i++) {
                String field = paramNames.get(i);
                if (field.equals("pageNumb")) {
                    Object numb = args[i];
                    Integer pageNumb = Integer.valueOf(numb != null ? numb.toString() : "0");
                    pageNumb = Pagination.handlePageNumb(pageNumb);
                    args[i] = pageNumb;
                }
                if (field.equals("pageSize")) {
                    Object size = args[i];
                    Integer pageSize = Integer.valueOf(size != null ? size.toString() : "0");
                    pageSize = Pagination.handlePageSize(pageSize);
                    args[i] = pageSize;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return joinPoint.proceed(args);
    }

    private List<String> getFieldsName(Class clazz, String clazzName, String methodName, Class<?>[] parameterTypes) {
        List<String> list = new ArrayList<>();

        // spring-core 方式获取形参名
        Method[] methods = clazz.getMethods();
        Method current = null;
        loopMethod: for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (method.getParameterTypes().length != parameterTypes.length) {
                continue;
            }
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class clsA = method.getParameterTypes()[i];
                Class clsB = parameterTypes[i];
                if (!clsA.getName().equals(clsB.getName())) {
                    continue loopMethod;
                }
            }
            current = method;
        }

        assert current != null : "Can not find method " + methodName + " in class " + clazzName;

        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        String[] parameterNames = pnd.getParameterNames(current);
        list.addAll(Arrays.asList(parameterNames));
        return list;
    }


}
