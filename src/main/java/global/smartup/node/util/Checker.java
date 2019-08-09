package global.smartup.node.util;

import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.PoConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Checker {

    private static final List<BigInteger> GasPriceList = Arrays.asList(
        BigInteger.valueOf(5L),
        BigInteger.valueOf(10L),
        BigInteger.valueOf(20L),
        BigInteger.valueOf(30L),
        BigInteger.valueOf(50L)
    );

    public static boolean isGasPriceRight(BigInteger gasPrice) {
        if (gasPrice != null) {
            for (BigInteger bigInteger : GasPriceList) {
                if (bigInteger.compareTo(gasPrice) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

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
