package global.smartup.node.util;

public class RespCode {

    public static class Market {

        public static final String MarketNotExist = "MarketNotExist";

    }

    public static class Trade {

        public static final String PriceCanNotLessZero = "PriceCanNotLessZero";

        public static final String VolumeCanNotLessZero = "VolumeCanNotLessZero";

        public static final String TypeError = "TypeError";

        public static final String GasPriceError = "GasPriceError";


        public static final String NewOrderNull = "NewOrderNull";

        public static final String SignError = "";

        public static class UpdateSell {

            public static final String OrderIdNull = "OrderIdNull";

        }

    }

}
