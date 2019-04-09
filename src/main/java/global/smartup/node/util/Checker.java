package global.smartup.node.util;

import org.apache.commons.lang3.StringUtils;

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

}
