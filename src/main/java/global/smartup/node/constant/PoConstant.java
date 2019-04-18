package global.smartup.node.constant;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoConstant {

    public class Market {

        public class Stage {

            public static final String Creating = "creating";

            public static final String Built = "built";

            public static final String Fail = "fail";

            public static final String Close = "close";

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

            public static final String Padding = "pending";

            public static final String Success = "success";

            public static final String Fail = "fail";

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

}
