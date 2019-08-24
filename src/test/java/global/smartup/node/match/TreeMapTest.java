package global.smartup.node.match;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.TreeMap;

public class TreeMapTest {

    public static void main(String[] args) {

        TreeMap<BigDecimal, String> map = new TreeMap<>((a, b) -> a.compareTo(b) * -1);

        for (int i = 0; i < 100; i++) {
            BigDecimal random = BigDecimal.valueOf(Math.random() * 100).setScale(4, BigDecimal.ROUND_DOWN);
            map.put(random, random.toPlainString());
        }
        BigDecimal test = BigDecimal.valueOf(100.55);
        test = test.setScale(4, BigDecimal.ROUND_DOWN);
        map.put(test, test.toPlainString());


        System.out.println("first key " + map.firstKey());


        for (BigDecimal k : map.keySet()) {
            System.out.print(k.toPlainString() + ", ");
        }
        System.out.println("");

        int temp = 0;
        Iterator<BigDecimal> iterator = map.keySet().iterator();
        while (iterator.hasNext() && temp < 10) {
            BigDecimal n = iterator.next();
            System.out.print(map.get(n) + ", ");
            temp++;
        }
        System.out.println("");

    }
}
