package global.smartup.node.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;

public class Wrapper implements Serializable {

    /**
     * {@link WpConstant}
     */
    private String code;

    private String msg;

    private Object obj;

    public Wrapper(String code, Object obj, String message) {
        this.code = code;
        this.obj = obj;
        this.msg = message;
    }

    public static Wrapper success() {
        return new Wrapper(WpConstant.Code.Success, null,  WpConstant.Message.Success);
    }

    public static Wrapper success(Object obj) {
        return new Wrapper(WpConstant.Code.Success, obj,  WpConstant.Message.Success);
    }

    public static Wrapper error(String code, String msg) {
        return new Wrapper(code, null,  msg);
    }

    public static Wrapper alert(String message) {
        return new Wrapper(WpConstant.Code.Alert, null, message);
    }

    public static Wrapper sysError() {
        return new Wrapper(WpConstant.Code.SystemError, null, WpConstant.Message.SystemError);
    }

    public static Wrapper notLogin() {
        return new Wrapper(WpConstant.Code.NoTLogin, null, WpConstant.Message.NotLogin);
    }

    public static Wrapper paramError(Object o) {
        return new Wrapper(WpConstant.Code.ParamError, o, WpConstant.Message.ParamError);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String toJsonString() {
        return JSON.toJSONString(this, SerializerFeature.WriteMapNullValue);
    }

}
