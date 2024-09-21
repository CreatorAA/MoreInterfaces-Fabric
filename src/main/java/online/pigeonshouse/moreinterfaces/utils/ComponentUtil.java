package online.pigeonshouse.moreinterfaces.utils;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ComponentUtil {
    public static MutableComponent literal(String text) {
        return new TextComponent(text);
    }

    public static MutableComponent translatable(String s, Object... objects) {
        return new TranslatableComponent(s, objects);
    }
}
