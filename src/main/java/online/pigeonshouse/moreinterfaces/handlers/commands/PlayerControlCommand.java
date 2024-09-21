package online.pigeonshouse.moreinterfaces.handlers.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerControlTask;
import online.pigeonshouse.moreinterfaces.eventhandler.ServerLifecycleEventHandler;
import online.pigeonshouse.moreinterfaces.handlers.MIServerPlayer;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数：
 * <p>
 * 玩家 - player
 * 是否只控制假人（默认为true） - isDummy
 * 行为类型 - actionType
 * -使用物品 use
 * -执行一次 once（默认）
 * -持续 continue
 * -间隔 interval
 * <p>
 * -攻击 attack
 * -执行一次 once（默认）
 * -持续 continue
 * -间隔 interval
 * <p>
 * -移动 move
 * -持续 continue
 * -距离 distance
 * <p>
 * -跳跃 jump
 * -执行一次 once（默认）
 * -持续 continue
 * <p>
 * -丢弃物品 drop
 * -执行一次 once
 * -丢弃整组 stack
 * -持续 continue
 * <p>
 * -交换左右手 swapHands
 * -潜行 sneak
 * -启动\停止 start\stop （默认情况自动判断）
 * -死亡 kill
 * -看 look
 * -方向 direction
 * -坐标 pos
 * <p>
 * -停止所有行为 stop
 * <p>
 * -将身上所有物品放入指定容器 putBox
 * -从容器中取出所有物品 takeBox
 */
public class PlayerControlCommand implements CommandHandler {
    public static final Map<String, PlayerControlTask> tickTask = new ConcurrentHashMap<>();

    public static void check() {
        MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();
        tickTask.forEach((name, task) -> {
            ServerPlayer player = server.getPlayerList().getPlayerByName(name);
            if (player == null || player.hasDisconnected()) {
                tickTask.remove(name);
                ServerLifecycleEventHandler.INSTANCE.removeTickTask(task);
                return;
            }

            if (task.getSynthesizer().isEmpty()) {
                tickTask.remove(name);
                ServerLifecycleEventHandler.INSTANCE.removeTickTask(task);
            }
        });
    }

    public static void remove(String name) {
        PlayerControlTask remove = tickTask.remove(name);
        if (remove != null)
            ServerLifecycleEventHandler.INSTANCE.removeTickTask(remove);
    }

    public static final PlayerControlCommand INSTANCE = new PlayerControlCommand();

    private PlayerControlCommand() {
    }

    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();
        boolean isDummy = map.getBooleanOrDef("isDummy", true);
        String playerName = map.getString(session, "player");
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            session.sendErrorMsg(MessageCode.ENTITY_NOT_FOUND, "玩家不存在");
            return;
        }
        boolean dummy = player instanceof MIServerPlayer;

        if (isDummy && !dummy) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "无法操作非假人玩家！");
            return;
        }

        String actionType = map.getString(session, "actionType");
        PlayerControlTask control = tickTask.get(playerName);
        PlayerControlTask.Behavior behavior = buildBehavior(dummy, actionType, session, map);

        if (behavior == null) {
            if (actionType.equals("stop")) {
                if (control != null) {
                    remove(playerName);
                }
                session.sendSucceededMessage();
                return;
            } else if (dummy && actionType.equals("kill")) {
                player.kill();
                session.sendSucceededMessage();
                return;
            }

            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT,
                    String.format("%s指令无法操作非假人玩家或指令本身不存在！", actionType));
            return;
        }

        if (control != null) {
            control.getSynthesizer().add(behavior.action(), behavior);
            session.sendSucceededMessage();
            return;
        }

        PlayerControlTask controlTask = new PlayerControlTask(session, player);
        controlTask.getSynthesizer().add(behavior.action(), behavior);
        tickTask.put(playerName, controlTask);

        if (!ServerLifecycleEventHandler.INSTANCE.addTickTask(controlTask)) {
            session.sendErrorMsg(MessageCode.SESSION_IN_USE, "该会话ID已被其他人使用，请更换会话ID");
            return;
        }

        session.sendSucceededMessage();
    }

    private PlayerControlTask.Behavior buildBehavior(boolean dummy, String actionType,
                                                     ChatSession session, CommandMap map) {
        switch (actionType) {
            case "use":
                return buildUseBehavior(session, map);
            case "attack":
                return buildAttackBehavior(session, map);
            case "jump":
                if (!dummy) return null;
                return buildJumpBehavior(session, map);
        }
        return null;
    }

    private PlayerControlTask.Behavior buildJumpBehavior(ChatSession session, CommandMap map) {
        PlayerControlTask.Behavior.Jump jump = new PlayerControlTask.Behavior.Jump();
        String executeType = map.getStringOrDef("executeType", "once");
        switch (executeType) {
            case "continue":
                jump.setCount(map.getIntOrDef(session, "count", -1));
                break;
            case "interval":
                int tick = map.getIntOrDef(session, "tick", 1);
                jump.setCount(map.getIntOrDef(session, "count", -1));
                jump.setInterval(tick);
                break;
        }
        return jump;
    }

    private PlayerControlTask.Behavior buildAttackBehavior(ChatSession session, CommandMap map) {
        PlayerControlTask.Behavior.Attack attack = new PlayerControlTask.Behavior.Attack();
        String executeType = map.getStringOrDef("executeType", "once");
        switch (executeType) {
            case "continue":
                attack.setCount(map.getIntOrDef(session, "count", -1));
                break;
            case "interval":
                int tick = map.getIntOrDef(session, "tick", 1);
                attack.setCount(map.getIntOrDef(session, "count", -1));
                attack.setInterval(tick);
                break;
        }
        return attack;
    }

    private PlayerControlTask.Behavior buildUseBehavior(ChatSession session, CommandMap map) {
        PlayerControlTask.Behavior.Use use = new PlayerControlTask.Behavior.Use();
        String executeType = map.getStringOrDef("executeType", "once");
        switch (executeType) {
            case "continue":
                use.setCount(map.getIntOrDef(session, "count", -1));
                break;
            case "interval":
                int tick = map.getIntOrDef(session, "tick", 1);
                use.setCount(map.getIntOrDef(session, "count", -1));
                use.setInterval(tick);
                break;
        }
        return use;
    }
}
