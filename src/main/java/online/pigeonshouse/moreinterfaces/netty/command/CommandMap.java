package online.pigeonshouse.moreinterfaces.netty.command;

import com.google.gson.JsonSyntaxException;
import online.pigeonshouse.moreinterfaces.handlers.MIData;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.GsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommandMap extends HashMap<String, Object> {
    public CommandMap() {
    }

    public CommandMap(Map<String, Object> map) {
        super(map);
    }

    /**
     * 获取字符串，如果value不存在则抛出异常
     */
    public String getString(ChatSession session, String key) {
        Object value = get(key);
        if (Objects.isNull(value)) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，缺少参数：" + key);
            throw new CommandArgException();
        }

        return value.toString();
    }

    public String getStringOrDef(String key, String def) {
        return getOrDefault(key, def).toString();
    }

    public long getLong(ChatSession session, String overlay) {
        try {
            return Long.parseLong(getString(session, overlay));
        }catch (NumberFormatException e) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，参数类型错误：" + overlay + "应为long");
            throw e;
        }
    }

    public long getLongOrDef(ChatSession session, String overlay, long def) {
        try {
            return Long.parseLong(getStringOrDef(overlay, Long.toString(def)));
        }catch (NumberFormatException e) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，参数类型错误：" + overlay + "应为long");
            throw e;
        }
    }

    public int getInt(ChatSession session, String overlay) {
        String string = getString(session, overlay);
        try {
            return Integer.parseInt(string);
        }catch (NumberFormatException e) {
            try {
                return (int)Math.round(Double.parseDouble(string));
            }catch (NumberFormatException e1) {
                session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，参数类型错误：" + overlay + "应为int");
                throw e;
            }
        }
    }

    public int getIntOrDef(ChatSession session, String overlay, int def) {
        String orDef = getStringOrDef(overlay, Integer.toString(def));
        try {
            return Integer.parseInt(orDef);
        }catch (NumberFormatException e) {
            try {
                return (int)Math.round(Double.parseDouble(orDef));
            }catch (NumberFormatException e1) {
                session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，参数类型错误：" + overlay + "应为int");
                throw e;
            }
        }
    }

    public double getDouble(ChatSession session, String overlay) {
        try {
            return Double.parseDouble(getString(session, overlay));
        }catch (NumberFormatException e) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，参数类型错误：" + overlay + "应为double");
            throw e;
        }
    }

    public double getDoubleOrDef(ChatSession session, String overlay, double def) {
        try {
            return Double.parseDouble(getStringOrDef(overlay, Double.toString(def)));
        }catch (NumberFormatException e) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，参数类型错误：" + overlay + "应为double");
            throw e;
        }
    }

    public boolean getBoolean(ChatSession session, String overlay) {
        return Boolean.parseBoolean(getString(session, overlay));
    }

    public boolean getBooleanOrDef(String overlay, boolean def) {
        return Boolean.parseBoolean(getStringOrDef(overlay, Boolean.toString(def)));
    }

    public <T> T formGson(ChatSession session, String key, Class<T> clazz) {
        try {
            return GsonUtil.GSON.fromJson(getString(session, key), clazz);
        }catch (JsonSyntaxException e) {
            session.sendErrorMsg(MessageCode.JSON_DESERIALIZE_ERROR, "参数" + key + "反序列化失败，请检查格式！");
            throw e;
        }
    }

    public float getFloat(ChatSession session, String yaw) {
        try {
            return Float.parseFloat(getString(session, yaw));
        }catch (NumberFormatException e) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，参数类型错误：" + yaw + "应为float");
            throw e;
        }
    }

    public float getFloatOrDef(ChatSession session, String key, float def) {
        try {
            return Float.parseFloat(getStringOrDef(key, Float.toString(def)));
        }catch (NumberFormatException e) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "指令错误，参数类型错误：" + key + "应为float");
            throw e;
        }
    }
}
