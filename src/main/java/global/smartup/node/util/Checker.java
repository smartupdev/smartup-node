package global.smartup.node.util;

import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.PoConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

public class Checker {

    public static boolean isAddress(String address) {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        if (address.length() != 42) {
            return false;
        }
        if (!address.startsWith("0x")) {
            return false;
        }
        return true;
    }

    public static boolean isTxHash(String txHash) {
        if (StringUtils.isBlank(txHash)) {
            return false;
        }
        if (txHash.length() != 66) {
            return false;
        }
        return true;
    }

    public static boolean isNodeSegment(String segment) {
        if (PoConstant.KLineNode.Segment.Hour.equals(segment)
                || PoConstant.KLineNode.Segment.Day.equals(segment)
                || PoConstant.KLineNode.Segment.Week.equals(segment)) {
            return true;
        }
        return false;
    }

    public static boolean isNodeSegmentTimeId(String segment, String timeId) {
        try {
            if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
                DateUtils.parseDate(timeId, BuConstant.KlineIdFormatHour);
            } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
                DateUtils.parseDate(timeId, BuConstant.KlineIdFormatDay);
            } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
                DateUtils.parseDate(timeId, BuConstant.KlineIdFormatWeek);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
