package global.smartup.node.constant;

public class PoConstant {


    public static class TxStage {

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

        }

        public static class TopType {

            public static final String Hottest = "hottest";

            public static final String Newest = "newest";

            public static final String Populous = "populous";

            public static final String Richest = "richest";

        }

    }

    public class Post {

        public class Type {

            public static final String Root = "root";

            public static final String Market = "market";

        }

    }

    public class Trade {

        public class Stage {

            // public static final String Padding = "pending";
            //
            // public static final String Success = "success";
            //
            // public static final String Fail = "fail";

        }

        public class Type {

            public static final String Buy = "buy";

            public static final String Sell = "sell";

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

            public static final String[] All = new String[]{Market, Post};

        }

    }

    public static class Notification {

        public static class Type {

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


}
