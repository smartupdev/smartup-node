package global.smartup.node.util;

import global.smartup.node.constant.PoConstant;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CommonTest {

    @Test
    public void parseInput() {
        String input = "0x000000000000000000000000437098700e7de348e436b809c74bb2442abd3bd60000000000000000000000000000000000000000000000878678326eac900000000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000001";

        List<Type> params =  FunctionReturnDecoder.decode(input, Arrays.asList(new TypeReference[]{
                TypeReference.create(Address.class),
                TypeReference.create(Uint256.class),
                TypeReference.create(DynamicBytes.class)
        }));

        for (Type param : params) {
            System.out.println(param.getTypeAsString() + " : " + param.getValue().toString());
        }

        DynamicBytes bytes = (DynamicBytes) params.get(2);
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes.getValue()) {

            String s = Integer.toHexString(b);
            if (s.length() < 2) {
                s = "0" + s;
            }
            // System.out.println(s);
            sb.append(s);
        }
        System.out.println(sb.toString());
    }

    @Test
    public void getTimeIdInRange() {
        List<String> list = Common.getTimeIdInRange("1week", "2019_04_01", "2019_04_16");
        list.forEach(System.out::println);
    }

    @Test
    public void getLastTimeId() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.YEAR, 2019);
        c.set(Calendar.MONTH, 1 - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);
        System.out.println(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(c.getTime()));

        String id = Common.getLastTimeId(PoConstant.KLineNode.Segment.Hour, c.getTime());
        System.out.println(id);
    }

    @Test
    public void getSevenDay6HourNode() {
        List<String> ret = Common.getSevenDay6HourNode();
        for (String s : ret) {
            System.out.println(s);
        }
    }

    @Test
    public void getNextTimeId() {
        String nextTimeId = Common.getNextTimeId(PoConstant.KLineNode.Segment.Week, new Date());
        System.out.println(nextTimeId);
    }

    @Test
    public void getEndTimeInSegment() {
        Date date = Common.getEndTimeInSegment(PoConstant.KLineNode.Segment.Week, Common.parseSimpleTime("2018-12-30 00:00:00"));
        System.out.println(DateFormatUtils.format(date, Common.SimpleFormatter));
    }

    @Test
    public void getNextTime() {
        Date d = Common.getNextTime("1hour", new Date());
        System.out.println(Common.formatSimpleTime(d));
    }

}
