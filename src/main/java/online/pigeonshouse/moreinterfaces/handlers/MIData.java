package online.pigeonshouse.moreinterfaces.handlers;


import online.pigeonshouse.moreinterfaces.utils.GsonUtil;

public abstract class MIData {

    public String toJsonString() {
        return GsonUtil.GSON.toJson(this);
    }
}
