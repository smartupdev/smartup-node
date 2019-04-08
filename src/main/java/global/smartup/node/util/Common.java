package global.smartup.node.util;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.math.BigInteger;

public class Common {

    public static final String letters = "abcdefghijklmnopqrstuvwxyz";

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

}
