package online.pigeonshouse.moreinterfaces.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
    public static final Gson GSON = new Gson();
    public static final Gson FORMATTER_GSON = new GsonBuilder()
            .setPrettyPrinting().create();
}
