package global.smartup.node.util;

import global.smartup.node.constant.PoConstant;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

public class Common {

    public static final String letters = "abcdefghijklmnopqrstuvwxyz";


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
                lastTimeId = DateFormatUtils.format(c.getTime(), "yyyy_MM_dd_HH");
            } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, -1);
                lastTimeId = DateFormatUtils.format(c.getTime(), "yyyy_MM_dd");
            } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.setFirstDayOfWeek(Calendar.SUNDAY);
                c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
                c.add(Calendar.DAY_OF_YEAR, -7);
                lastTimeId = DateFormatUtils.format(c.getTime(), "yyyy_MM_dd");
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
            timeId = DateFormatUtils.format(date, "yyyy_MM_dd_HH");
        } else if (segment.equals(PoConstant.KLineNode.Segment.Day)) {
            timeId = DateFormatUtils.format(date, "yyyy_MM_dd");
        } else if (segment.equals(PoConstant.KLineNode.Segment.Week)) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.setFirstDayOfWeek(Calendar.SUNDAY);
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
            timeId = DateFormatUtils.format(c.getTime(), "yyyy_MM_dd");
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


}
