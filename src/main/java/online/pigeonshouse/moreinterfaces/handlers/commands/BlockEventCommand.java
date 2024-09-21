package online.pigeonshouse.moreinterfaces.handlers.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.eventhandler.AreaScanTask;
import online.pigeonshouse.moreinterfaces.eventhandler.BlockStateEventHandler;
import online.pigeonshouse.moreinterfaces.eventhandler.ServerLifecycleEventHandler;
import online.pigeonshouse.moreinterfaces.handlers.MIArea;
import online.pigeonshouse.moreinterfaces.handlers.MIBlock;
import online.pigeonshouse.moreinterfaces.handlers.MIPos;
import online.pigeonshouse.moreinterfaces.handlers.message.BlockEventMessage;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.ConnectionManage;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.WorldUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BlockEventCommand implements CommandHandler {
    public static final Map<String, Map<String, EventCallback>> subscribeEvent = new HashMap<>();
    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);

    public static final BlockEventCommand INSTANCE = new BlockEventCommand();

    private BlockEventCommand() {
    }

    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        String event = map.getString(session, "event");
        EventCallback eventCallback = new EventCallback(session, event);

        if (subscribeEvent.containsKey(event)) {
            Map<String, EventCallback> callbackMap = subscribeEvent.get(event);
            EventCallback old = callbackMap.get(session.sessionId());

            if (Objects.nonNull(old)) {
                if (ConnectionManage.hasUser(old.session().channel())) {
                    if (old.session().channel() == session.channel()) {
                        session.sendErrorMsg(MessageCode.ALREADY_SUBSCRIBED, "您已经订阅了该事件！如需重复订阅，请更换会话ID");
                        return;
                    }
                    session.sendErrorMsg(MessageCode.SESSION_IN_USE, "该会话ID已被其他人使用，请更换会话ID");
                    return;
                }
            }

            callbackMap.put(session.sessionId(), eventCallback);
        } else {
            subscribeEvent.computeIfAbsent(event, k -> new HashMap<>())
                    .put(session.sessionId(), eventCallback);
        }

        try {
            if (activateListeners(session, event, map)) {
                session.sendSucceededMessage();
            } else {
                subscribeEvent.get(event)
                        .remove(eventCallback.session().sessionId());
            }
        } catch (Exception e) {
            subscribeEvent.get(event)
                    .remove(eventCallback.session().sessionId());
        }
    }

    public static class UnsubscribeBlockEventCommand implements CommandHandler {
        @Override
        public void onCommand(ChatSession session, CommandMap map) {
            String event = map.getString(session, "event");
            if (!subscribeEvent.containsKey(event)) {
                session.sendErrorMsg(MessageCode.NOT_SUBSCRIBED, "您没有订阅该事件！");
                return;
            }

            String sessionId = map.getStringOrDef("sessionId", session.sessionId());

            EventCallback eventCallback = subscribeEvent.get(event).get(sessionId);

            if (Objects.isNull(eventCallback) ||
                    (eventCallback.session().channel() != session.channel())) {
                session.sendErrorMsg(MessageCode.NOT_SUBSCRIBED, "您没有订阅该事件或sessionId不一致！");
                return;
            }

            subscribeEvent.get(event).remove(sessionId);
        }
    }

    public static class EventCallback {
        final ChatSession session;
        final String event;

        public EventCallback(ChatSession session, String event) {
            this.session = session;
            this.event = event;
        }

        public ChatSession session() {
            return session;
        }

        public String event() {
            return event;
        }

        public void call(MIBlock block, Map<String, Object> data) {
            if (ConnectionManage.hasUser(session().channel())) {
                session().sendMsg(new BlockEventMessage(session().sessionId(), event, block, data));
            } else {
                subscribeEvent.get(event).remove(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EventCallback)) return false;
            EventCallback that = (EventCallback) o;
            return Objects.equals(event, that.event) && Objects.equals(session, that.session);
        }

        @Override
        public int hashCode() {
            return Objects.hash(session, event);
        }
    }

    public static boolean callEvent(ChatSession session, String event, MIBlock block, Map<String, Object> data) {
        Map<String, EventCallback> callbackMap = subscribeEvent.get(event);
        if (Objects.nonNull(callbackMap)) {
            if (ConnectionManage.hasUser(session.channel())) {
                EventCallback eventCallback = callbackMap.get(session.sessionId());
                if (Objects.nonNull(eventCallback) && eventCallback.session().channel() == session.channel()) {
                    try {
                        eventCallback.call(block, data);
                    } catch (Exception e) {
                        log.error("调用监听器时发生错误！", e);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static final int maxArea = 16 * 16 * 9 * 20;

    public static boolean activateListeners(ChatSession session, String event, CommandMap map) {
        MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();

        switch (event) {
            case "block_state":
                MIPos pos = MIPos.create(map.getStringOrDef("world", ""),
                        map.getInt(session, "x"), map.getInt(session, "y"), map.getInt(session, "z"));

                ServerLevel level1 = pos.getWorld().isEmpty() ? server.overworld() :
                        WorldUtil.getLevel(server.overworld(), pos.getWorld());

                if (level1 == null) {
                    session.sendErrorMsg(MessageCode.LEVEL_NOT_FOUND, "找不到世界：" + pos.getWorld());
                    return false;
                }

                BlockStateEventHandler handler = new BlockStateEventHandler(session, pos, level1);

                return addTask(session, handler);
            case "area_entities_info":
                MIPos pos1 = map.formGson(session, "pos1", MIPos.class);
                MIPos pos2 = map.formGson(session, "pos2", MIPos.class);

                if (!pos1.getWorld().equals(pos2.getWorld())) {
                    session.sendErrorMsg(MessageCode.WORLD_NOT_EQUAL, "两个坐标必须在同一个世界！");
                    return false;
                }

                ServerLevel level2 = WorldUtil.getLevel(server.overworld(),
                        pos1.getWorld());
                if (level2 == null) {
                    session.sendErrorMsg(MessageCode.LEVEL_NOT_FOUND, "找不到世界：" + pos1.getWorld());
                    return false;
                }

                int width = Math.max(pos1.getIntX(), pos2.getIntX()) - Math.min(pos1.getIntX(), pos2.getIntX());
                int height = Math.max(pos1.getIntY(), pos2.getIntY()) - Math.min(pos1.getIntY(), pos2.getIntY());
                int length = Math.max(pos1.getIntZ(), pos2.getIntZ()) - Math.min(pos1.getIntZ(), pos2.getIntZ());
                int area = width * height * length;

                if (width * height > maxArea) {
                    session.sendErrorMsg(MessageCode.AREA_TOO_LARGE, String.format("区域总面积不应该超过：%s, 您选择的面积：%s",
                            maxArea, area));
                    return false;
                }

                String name = map.getStringOrDef("name", null);
                MIArea miArea = new MIArea(pos1, pos2, name);

                AreaScanTask task = new AreaScanTask(session, miArea, level2);
                int tick = map.getIntOrDef(session, "tick", 20);
                task.setPeriod(tick);
                return addTask(session, task);
        }

        session.sendErrorMsg(MessageCode.NOT_IMPLEMENTED, "无法找到相关事件！");
        return false;
    }

    private static boolean addTask(ChatSession session, ServerLifecycleEventHandler.TickTask handler) {
        if (!ServerLifecycleEventHandler.INSTANCE.addTickTask(handler)) {
            ServerLifecycleEventHandler.TickTask tickTask =
                    ServerLifecycleEventHandler.INSTANCE.getTask(session.sessionId());

            if (ConnectionManage.hasUser(tickTask.getSession().channel())) {
                if (tickTask.getSession().channel() == session.channel()) {
                    session.sendErrorMsg(MessageCode.ALREADY_SUBSCRIBED, "您已经订阅了该事件！如需重复订阅，请更换会话ID");
                    return false;
                }
                session.sendErrorMsg(MessageCode.SESSION_IN_USE, "该会话ID已被其他人使用，请更换会话ID");
                return false;
            }

            ServerLifecycleEventHandler.INSTANCE.removeTickTask(tickTask);
            ServerLifecycleEventHandler.INSTANCE.addTickTask(handler);
        }
        return true;
    }
}
