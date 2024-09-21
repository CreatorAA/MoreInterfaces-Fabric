package online.pigeonshouse.moreinterfaces.utils;

import java.util.UUID;

public class UUIDUtil {
    /**
     * 传入字符串形式的uuid，返回一个UUID对象
     */
    public static UUID getUUID(String uuidStr) {
        return UUID.fromString(uuidStr);
    }
}
