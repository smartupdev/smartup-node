package global.smartup.node.constant;

public class RedisKey {

    public static final String UserTokenPrefix = "sn_token_address:";

    public static final Integer KlineExpire = 5 * 60 * 1000;

    public static final String KlinePrefix = "sn_kline:";

    public static final String KlineNoDataFlag = "no_data";


    public static final Integer NotificationExpire = 5 * 60 * 1000;

    public static final String NotificationPrefix = "sn_notification:";

    public static final String NotificationCountPrefix = ":count";

    public static final String NotificationListPrefix = ":list";

}
