package global.smartup.node.util;

import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.PoConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

public class Common {

    public static final String letters = "abcdefghijklmnopqrstuvwxyz";

    public static final String SimpleFormatter = "yyyy-MM-dd HH:mm:ss";

    public static String formatSimpleTime(Date time) {
        return DateFormatUtils.format(time, SimpleFormatter);
    }

    public static Date parseSimpleTime(String time) {
        try {
            return DateUtils.parseDate(time, SimpleFormatter);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    public static int getRandomChar(int count) {
        return (int) Math.round(Math.random() * (count));
    }

    public static String getRandomString(int length){
        StringBuffer sb = new StringBuffer();
        int len = letters.length();
        for (int i = 0; i < length; i++) {
            sb.append(letters.charAt(getRandomChar(len-1)));
        }
        return sb.toString();
    }

    public static String getPublicKeyInHex(String privateKeyInHex){
        BigInteger privateKeyInBT = new BigInteger(privateKeyInHex, 16);
        ECKeyPair pair = ECKeyPair.create(privateKeyInBT);
        String publicKeyInHex = "0x" + Keys.getAddress(pair);
        return publicKeyInHex;
    }

    public static Date fillZero(String segment, Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTime();
        } else if (segment.equals(PoConstant.KLineNode.Segment.Day)
                || segment.equals(PoConstant.KLineNode.Segment.Week)) {
            c.set(Calendar.HOUR, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTime();
        }
        return date;
    }

    public static String getNextTimeId(String segment, Date date) {
        String nextTimeId = null;
        if (segment == null || date == null) {
            return nextTimeId;
        }
        try {
            if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.HOUR_OF_DAY, 1);
                nextTimeId = DateFormatUtils.format(c.getTime(), BuConstant.KlineIdFormatHour);
            } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, 1);
                nextTimeId = DateFormatUtils.format(c.getTime(), BuConstant.KlineIdFormatDay);
            } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.setFirstDayOfWeek(Calendar.SUNDAY);
                c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
                c.add(Calendar.DAY_OF_YEAR, 7);
                nextTimeId = DateFormatUtils.format(c.getTime(), BuConstant.KlineIdFormatWeek);
            }
        } catch (Exception e) {
            return nextTimeId;
        }
        return nextTimeId;
    }

    public static Date getNextTime(String segment, Date date) {
        if (StringUtils.isBlank(segment) || date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
            c.setTime(date);
            c.add(Calendar.HOUR_OF_DAY, 1);
            return c.getTime();
        } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
            c.setTime(date);
            c.add(Calendar.DAY_OF_MONTH, 1);
            return c.getTime();
        } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
            c.setTime(date);
            c.setFirstDayOfWeek(Calendar.SUNDAY);
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
            c.add(Calendar.DAY_OF_YEAR, 7);
        }
        return c.getTime();
    }

    public static String getLastTimeId(String segment, Date date) {
        String lastTimeId = null;
        if (segment == null || date == null) {
            return lastTimeId;
        }
        try {
            if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.HOUR_OF_DAY, -1);
                lastTimeId = DateFormatUtils.format(c.getTime(), BuConstant.KlineIdFormatHour);
            } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, -1);
                lastTimeId = DateFormatUtils.format(c.getTime(), BuConstant.KlineIdFormatDay);
            } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.setFirstDayOfWeek(Calendar.SUNDAY);
                c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
                c.add(Calendar.DAY_OF_YEAR, -7);
                lastTimeId = DateFormatUtils.format(c.getTime(), BuConstant.KlineIdFormatWeek);
            }
        } catch (Exception e) {
            return lastTimeId;
        }
        return lastTimeId;
    }

    public static String getTimeId(String segment, Date date) {
        String timeId = null;
        if (segment == null) {
            return timeId;
        }
        if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
            timeId = DateFormatUtils.format(date, BuConstant.KlineIdFormatHour);
        } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
            timeId = DateFormatUtils.format(date, BuConstant.KlineIdFormatDay);
        } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.setFirstDayOfWeek(Calendar.SUNDAY);
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
            timeId = DateFormatUtils.format(c.getTime(), BuConstant.KlineIdFormatWeek);
        }
        return timeId;
    }

    public static Date getDayStart(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date getDayEnd(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    public static Date getSomeDaysAgo(Date d, int some) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_YEAR, -some);
        return c.getTime();
    }

    public static Date getSomeHoursAgo(Date d, int some) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.HOUR, -some);
        return c.getTime();
    }

    public static List<String> getSevenDay6HourNode() {
        List<String> list = new ArrayList<>();

        Date end = new Date();
        Date start = Common.getSomeDaysAgo(end, 7);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.set(Calendar.HOUR, 0);

        while (calendar.getTime().getTime() <= end.getTime()) {
            String id = DateFormatUtils.format(calendar.getTime(), BuConstant.KlineIdFormatHour);
            list.add(id);
            calendar.add(Calendar.HOUR, 6);
        }

        if (list.size() > 28) {
            int remove = list.size() - 28;
            for (int i = 0; i < remove; i++) {
                list.remove(0);
            }
        }

        return list;
    }

    public static Date getEndTimeInSegment(String segment, Date time) {
        if (segment == null) {
            return null;
        }
        if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
            Calendar c = Calendar.getInstance();
            c.setTime(time);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            return c.getTime();
        } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
            Calendar c = Calendar.getInstance();
            c.setTime(time);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            return c.getTime();
        } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
            Calendar c = Calendar.getInstance();
            c.setTime(time);
            c.setFirstDayOfWeek(Calendar.SUNDAY);
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.add(Calendar.DAY_OF_YEAR, 6);
            return c.getTime();
        }
        return null;
    }

    public static Boolean isFuture(String segment, String timeId) {
        try {
            Date d;
            if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
                d = DateUtils.parseDate(timeId, BuConstant.KlineIdFormatHour);
            } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
                d = DateUtils.parseDate(timeId, BuConstant.KlineIdFormatDay);
            } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
                d = DateUtils.parseDate(timeId, BuConstant.KlineIdFormatWeek);
            } else {
                return null;
            }
            if (d.getTime() > new Date().getTime()) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            // e.printStackTrace();
            return null;
        }
    }

    public static List<String> getTimeIdInRange(String segment, String start, String end) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isAnyBlank(segment, start, end)) {
            return list;
        }
        Date current = new Date();
        Calendar calendar = Calendar.getInstance();
        try {

            if (segment.equals(PoConstant.KLineNode.Segment.Hour)) {
                Date s = DateUtils.parseDate(start, BuConstant.KlineIdFormatHour);
                Date e = DateUtils.parseDate(end, BuConstant.KlineIdFormatHour);
                if (s.getTime() > current.getTime() || s.getTime() > e.getTime()) {
                    return list;
                }
                calendar.setTime(s);
                while (calendar.getTime().getTime() <= current.getTime() && calendar.getTime().getTime() <= e.getTime()) {
                    String id = DateFormatUtils.format(calendar.getTime(), BuConstant.KlineIdFormatHour);
                    list.add(id);
                    calendar.add(Calendar.HOUR, 1);
                }
            } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
                Date s = DateUtils.parseDate(start, BuConstant.KlineIdFormatDay);
                Date e = DateUtils.parseDate(end, BuConstant.KlineIdFormatDay);
                if (s.getTime() > current.getTime() || s.getTime() > e.getTime()) {
                    return list;
                }
                calendar.setTime(s);
                while (calendar.getTime().getTime() <= current.getTime() && calendar.getTime().getTime() <= e.getTime()) {
                    String id = DateFormatUtils.format(calendar.getTime(), BuConstant.KlineIdFormatDay);
                    list.add(id);
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
            } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
                Date s = DateUtils.parseDate(start, BuConstant.KlineIdFormatWeek);
                Date e = DateUtils.parseDate(end, BuConstant.KlineIdFormatWeek);
                if (s.getTime() > current.getTime() || s.getTime() > e.getTime()) {
                    return list;
                }
                calendar.setTime(s);
                while (calendar.getTime().getTime() <= current.getTime() && calendar.getTime().getTime() <= e.getTime()) {
                    calendar.setFirstDayOfWeek(Calendar.SUNDAY);
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                    if (calendar.getTime().getTime() >= s.getTime() && calendar.getTime().getTime() <= e.getTime()) {
                        String id = DateFormatUtils.format(calendar.getTime(), BuConstant.KlineIdFormatWeek);
                        list.add(id);
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 7);
                }
            } else {
                return list;
            }
            return list;
        } catch (ParseException e) {
            // e.printStackTrace();
            return list;
        }
    }

    public static int getLenOfChinese(String s) {
        if (StringUtils.isBlank(s)) {
            return 0;
        } else {
            Pattern p = Pattern.compile("[^\u4e00-\u9fa5]");
            return p.matcher(s).replaceAll("").length();
        }
    }

    public static int getLenOfNotChinese(String s) {
        if (StringUtils.isBlank(s)) {
            return 0;
        } else {
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            return p.matcher(s).replaceAll("").length();
        }
    }

    public static boolean isRightMarketId(String marketId) {
        if (StringUtils.isBlank(marketId)) {
            return false;
        }
        if (8 > marketId.length() || marketId.length() > 15) {
            return false;
        }
        if (!isLetterDigit(marketId)) {
            return false;
        }
        return true;
    }

    public static boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }

}
