package global.smartup.node.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;
import java.util.Map;

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

    public static Wrapper create(Map<String, Object> map) {
        assert map.get("code") != null;
        Object msg = map.get("msg");
        return new Wrapper(map.get("code").toString(), map.get("obj"), msg != null ? msg.toString() : null);
    }

    public static Wrapper create(String code, Object obj) {
        return new Wrapper(code, obj, null);
    }

    public static Wrapper success() {
        return new Wrapper(WpConstant.Code.Success, null,  WpConstant.Message.Success);
    }

    public static Wrapper success(Object obj) {
        return new Wrapper(WpConstant.Code.Success, obj,  WpConstant.Message.Success);
    }

    public static Wrapper error(String code) {
        return new Wrapper(code, null,  null);
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
        return new Wrapper(WpConstant.Code.NotLogin, null, WpConstant.Message.NotLogin);
    }

    public static Wrapper paramError(Object o) {
        return new Wrapper(WpConstant.Code.ParamError, o, WpConstant.Message.ParamError);
    }

    public boolean wasSuccess() {
        return WpConstant.Code.Success.equals(code);
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
