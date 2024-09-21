package online.pigeonshouse.moreinterfaces.netty.message;

public class MessageCode {
    // 无权
    public static final int NO_PERMISSION = -2;

    // 服务器内部错误
    public static final int UNKNOWN_ERROR = -1;

    // 会话错误
    public static final int SESSION_ERROR = 1;

    // 认证错误
    public static final int AUTH_ERROR = 2;

    // 指令执行错误
    public static final int INSTRUCTION_ERROR = 3;

    // Minecraft未初始化
    public static final int MINECRAFT_NOT_INITIALIZED = 4;

    // 实体未找到
    public static int ENTITY_NOT_FOUND = 5;

    // 参数错误
    public static final int INVALID_ARGUMENT = 6;

    // 重复订阅
    public static final int ALREADY_SUBSCRIBED = 7;

    // 找不到世界
    public static final int LEVEL_NOT_FOUND = 8;

    // 找不到方块
    public static final int BLOCK_NOT_FOUND = 9;

    // 找不到容器
    public static final int CONTAINER_BLOCK_NOT_FOUND = 10;

    // 未实现
    public static final int NOT_IMPLEMENTED = 11;

    // 区块未加载
    public static final int CHUNK_NOT_LOADED = 12;

    // JSON反序列化错误
    public static final int JSON_DESERIALIZE_ERROR = 13;

    // 世界不匹配
    public static final int WORLD_NOT_EQUAL = 14;

    // 区域过大
    public static final int AREA_TOO_LARGE = 15;

    // 会话已使用
    public static final int SESSION_IN_USE = 16;

    // 未订阅
    public static final int NOT_SUBSCRIBED = 17;

    // 语法错误
    public static final int SYNTAX_ERROR = 18;
}
