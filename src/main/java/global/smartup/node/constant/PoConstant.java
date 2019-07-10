package global.smartup.node.constant;

public class PoConstant {

    public static class Currency {

        public static final String ETH = "ETH";

        public static final String SUT = "SUT";

    }

    public static class TxStage {

        @Deprecated
        public static final String Creating = "creating";

        public static final String Pending = "pending";

        public static final String Success = "success";

        public static final String Fail = "fail";


        public static boolean isFinish(String stage) {
            if (Success.equals(stage) || Fail.equals(stage)) {
                return true;
            }
            return false;
        }

    }

    public static class Market {

        public static class Status {

            public static final String Creating = "creating";

            public static final String Locked = "locked";

            public static final String Open = "open";

            public static final String Close = "close";

            public static final String Fail = "fail";

        }

        public static class TopType {

            public static final String Hottest = "hottest";

            public static final String Newest = "newest";

            public static final String Populous = "populous";

            public static final String Richest = "richest";

        }

    }

    public static class Post {

        public static class Type {

            public static final String Root = "root";

            public static final String Market = "market";

            public static boolean isType(String type) {
                if (Root.equals(type) || Market.equals(type)) {
                    return true;
                }
                return false;
            }

        }

    }

    public static class Trade {

        public static class Type {

            public static final String Buy = "buy";

            public static final String Sell = "sell";

            public static boolean isRight(String type) {
                if (Buy.equals(type) || Sell.equals(type)) {
                    return true;
                }
                return false;
            }

        }

    }

    public static class KLineNode {

        public static class Segment {

            // 1min 1hour 1day 1week

            public static final String Min = "1min";

            public static final String Hour = "1hour";

            public static final String Day = "1day";

            public static final String Week = "1week";

            public static final String[] All = new String[]{Hour, Day, Week};

        }

    }

    public static class Collect {

        public static class Type {

            public static final String Market = "market";

            public static final String Post = "post";

            public static final String Reply = "reply";

            public static final String[] All = new String[]{Market, Post, Reply};

        }

    }

    public static class Notification {

        public static class Style {

            public static final String System = "system";

            public static final String Personal = "personal";

        }

        public static class Type {

            public static final String ChargeSutFinish = "ChargeSutFinish";

            public static final String ChargeEthFinish = "ChargeEthFinish";

            public static final String WithdrawSutFinish = "WithdrawSutFinish";

            public static final String WithdrawEthFinish = "WithdrawEthFinish";

            public static final String MarketCreateFinish = "MarketCreateFinish";

            public static final String TradeFinish = "TradeFinish";

            public static final String ProposalSutCreateFinish = "ProposalSutCreateFinish";

            public static final String ProposalSutVoteFinish = "ProposalSutVoteFinish";

            public static final String ProposalSutFinish = "ProposalSutFinish";

            public static final String ProposalSuggestCreateFinish = "ProposalSuggestCreateFinish";

        }

    }

    public static class Proposal {

        public static class Type {

            public static final String Sut = "sut";

            public static final String Suggest = "suggest";

        }

    }

    public static class Liked {

        public static class Type {

            public static final String Post = "post";

            public static final String Reply = "reply";

        }

    }

    public static class Transaction {

        public static class Type {

            public static final String ChargeSut = "ChargeSut";

            public static final String ChargeEth = "ChargeEth";

            public static final String WithdrawSut = "WithdrawSut";

            public static final String WithdrawEth = "WithdrawEth";

            public static final String AdminWithdrawSut = "AdminWithdrawSut";

            public static final String AdminWithdrawEth = "AdminWithdrawEth";

            public static final String CreateMarket = "CreateMarket";

            public static final String BuyCT = "BuyCT";

            public static final String SellCT = "SellCT";

        }

    }

    public static class FundFlow {

        public static class Type {

            public static final String ChargeSut = "ChargeSut";

            public static final String ChargeEth = "ChargeEth";

            public static final String WithdrawSut = "WithdrawSut";

            public static final String WithdrawEth = "WithdrawEth";

            public static final String CreateMarket = "CreateMarket";

            public static final String AdminWithdrawSut = "AdminWithdrawSut";

            public static final String AdminWithdrawEth = "AdminWithdrawEth";

        }

        public static class Direction {

            public static final String In = "in";

            public static final String Out = "out";

        }

    }

}
