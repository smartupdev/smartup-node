package global.smartup.node.constant;

public class RedisKey {

    public static final String NoDataFlag = "no_data";



    public static final String UserTokenPrefix = "sn_token_address:";

    public static final Integer KlineExpire = 5 * 60 * 1000;

    public static final String KlinePrefix = "sn_kline:";



    public static final Integer NotificationExpire = 5 * 60 * 1000;

    public static final String NotificationPrefix = "sn_notification:";

    public static final String NotificationCountPrefix = ":count";

    public static final String NotificationListPrefix = ":list";



    public static class MarketTopUser {

        public static final Integer Expire = 1 * 60 * 1000;

        public static final String Prefix = "sn_market_top_users:";

        public static final String CTTopPrefix = ":top_ct_users";

        public static final String LikeTopPrefix = ":top_like_users";

        public static final String PostTopPrefix = ":top_post_users";

    }

}


