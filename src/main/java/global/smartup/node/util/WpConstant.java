package global.smartup.node.util;

/**
 * {@link global.smartup.node.util.Wrapper}
 * 包裹返回的常量
 */
public final class WpConstant {

    public static final class Code {

        // 成功
        public static final String Success = "0";
        // 系统异常
        public static final String SystemError = "1";
        // 自定义系统提示
        public static final String Alert = "2";
        // 未登陆
        public static final String NotLogin = "3";
        // 参数错误
        // 返回 obj = {"username": "长度超过10个"}
        public static final String ParamError = "4";


    }

    public static final class Message {

        public static final String Success = "success";
        public static final String SystemError = "system error";
        public static final String NotLogin = "not login";
        public static final String ParamError = "param error";


    }

}
