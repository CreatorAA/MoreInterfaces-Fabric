package online.pigeonshouse.moreinterfaces.netty.command;

import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.config.RemoteConfig;
import online.pigeonshouse.moreinterfaces.config.RemotePower;
import online.pigeonshouse.moreinterfaces.config.RemoteToken;
import online.pigeonshouse.moreinterfaces.handlers.commands.*;
import online.pigeonshouse.moreinterfaces.utils.PowerUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class CommandManage {
    private static final HashMap<String, Manage> groupMap = new HashMap<>();
    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);

    public static void init() {
        // 执行指令
        register("commands.minecraft", "executeMinecraftCommand", new MinecraftCommandExecuteCommand());
        register("commands.message", "sendMessage", new SendPlayerMsgCommand());
        register("commands.player", "createPlayer", new CreatePlayerCommand());
        register("commands.player.control", "controlPlayer", PlayerControlCommand.INSTANCE);

        // 查询信息
        register("query.entity", "getPlayerPos", new GetPlayerPosCommand());
        register("query.entity", "getPlayerList", new GetPlayerListCommand());
        register("query.block", "getBlockState", new QueryBlockCommand());
        register("query.world", "raycast", new GetRaycastCommand());

        // 监听
        register("event.player", "subscribePlayerEvent", PlayerEventCommand.INSTANCE);
        register("event.player", "unSubscribePlayerEvent", new PlayerEventCommand.UnsubscribePlayerEventCommand());
        register("event.block", "subscribeBlockEvent", BlockEventCommand.INSTANCE);
        register("event.block", "unSubscribeBlockEvent", new BlockEventCommand.UnsubscribeBlockEventCommand());
    }

    /**
     * 注册命令
     *
     * @param group   权限组
     * @param cmd     命令
     * @param handler 处理器
     */
    public static void register(String group, String cmd, CommandHandler handler) {
        if (!groupMap.containsKey(group)) {
            RemoteConfig.Config.INSTANCE.getRootPower()
                    .getGroups().add(group);

            groupMap.put(group, new Manage());
        }
        groupMap.get(group).putHandler(cmd, handler);
    }

    public static CommandHandler getCommand(String token, String cmd) {
        RemotePower power = Optional.ofNullable(PowerUtil.getPower(token))
                .orElse(RemotePower.GUEST);

        return getCommand(power, cmd);
    }

    public static CommandHandler getCommand(RemoteToken remoteToken, String cmd) {
        RemotePower power = Optional.ofNullable(PowerUtil.getPower(remoteToken))
                .orElse(RemotePower.GUEST);

        return getCommand(power, cmd);
    }

    public static CommandHandler getCommand(RemotePower power, String cmd) {
        if (power == RemotePower.GUEST) {
            return null;
        }

        for (String group : power.getGroups()) {
            CommandHandler handler;
            Manage manage = groupMap.get(group);
            if (Objects.isNull(manage)) {
                continue;
            }

            if (!Objects.isNull(handler = manage.getHandler(cmd))) {
                return handler;
            }
        }

        return null;
    }

    private static class Manage {
        private final HashMap<String, CommandHandler> commandMap = new HashMap<>();

        public CommandHandler getHandler(String cmd) {
            return commandMap.get(cmd);
        }

        public void putHandler(String cmd, CommandHandler handler) {
            if (commandMap.containsKey(cmd)) {
                throw new IllegalArgumentException("Command already registered: " + cmd);
            }

            commandMap.put(cmd, handler);
        }

        public void removeHandler(String cmd) {
            commandMap.remove(cmd);
        }

        public boolean contains(String cmd) {
            return commandMap.containsKey(cmd);
        }

        public HashSet<String> getCommands() {
            return new HashSet<>(commandMap.keySet());
        }
    }

}
