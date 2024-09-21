package online.pigeonshouse.moreinterfaces.utils;

import com.mojang.brigadier.StringReader;
import net.minecraft.network.chat.Component;

public class StringUtil {
    public static String generateKey(int length) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int num = (int) (Math.random() * 62);
            if (num < 10) {
                key.append(num);
            } else if (num < 36) {
                key.append((char) (num - 10 + 'A'));
            } else {
                key.append((char) (num - 36 + 'a'));
            }
        }
        return key.toString();
    }

    public static Component getText(String text) {
        StringReader stringReader = new StringReader(text);

        try {
            return Component.Serializer.fromJson(stringReader);
        }catch (Exception e) {
            e.printStackTrace();
            return ComponentUtil.literal(text);
        }
    }
}
