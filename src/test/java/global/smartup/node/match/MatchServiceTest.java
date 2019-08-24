package global.smartup.node.match;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MatchServiceTest {

    @Test
    public void test() {

        List<String> strings = new ArrayList<>();

        List<BigDecimal> list = new ArrayList<>();
        for (int i = 0; i < 10000 * 100; i++) {
            list.add(new BigDecimal(Math.random() * 100000));
        }
        System.out.println("list ok");

        long start = System.currentTimeMillis();

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal i : list) {
            sum = sum.add(i);
        }

        System.out.println("use time = " + (System.currentTimeMillis() - start));

    }

}
