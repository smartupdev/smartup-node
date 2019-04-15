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
    public void test() {
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

}
