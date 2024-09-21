package online.pigeonshouse.moreinterfaces.handlers;

import com.google.gson.Gson;

public abstract class MIData {
    public static final Gson GSON = new Gson();

    public String toJsonString() {
        return GSON.toJson(this);
    }
}
