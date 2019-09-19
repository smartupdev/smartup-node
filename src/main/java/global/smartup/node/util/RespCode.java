package global.smartup.node.util;

public class RespCode {

    public static final String Success = "0";

    public static final String NetWorkError = "NetWorkError";

    public static final String SignError = "SignError";

    public static class Account {

        public static final String SutNotEnough = "SutNotEnough";

        public static final String EthNotEnough = "EthNotEnough";

    }

    public static class Market {

        public static final String MarketNotExist = "MarketNotExist";

        public static final String MarketCanNotTrade = "MarketCanNotTrade";

        public static final String MarketIdError = "MarketIdError";

        public static final String MarketIdRepeat = "MarketIdRepeat";

        public static class CreateMarket {

            public static final String MarketIsCreating = "MarketIsCreating";

        }
    }

    public static class Trade {

        public static final String OrderNotExist = "OrderNotExist";

        public static final String OrderCanNotCancel = "OrderCanNotCancel";

        public static final String PriceCanNotLessZero = "PriceCanNotLessZero";

        public static final String VolumeCanNotLessZero = "VolumeCanNotLessZero";

        public static final String TypeError = "TypeError";

        public static final String StateError = "StateError";

        public static final String GasPriceError = "GasPriceError";

        public static final String NewOrderNull = "NewOrderNull";

        public static final String SignError = "SignError";

        public static final String SellSignError = "SellSignError";

        public static final String SutNotEnough = "SutNotEnough";

        public static final String EthNotEnough = "EthNotEnough";

        public static final String CtNotEnough = "CtNotEnough";

        public static final String NetWorkError = "";

        public static class UpdateSell {

            public static final String OrderIdNull = "OrderIdNull";

        }

        public static final String TakeSignError = "TakeSignError";

        public static final String MakeSignError = "MakeSignError";

    }

}
