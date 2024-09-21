package online.pigeonshouse.moreinterfaces.handlers.commands;

import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.eventhandler.EntityDeathEventHandler;
import online.pigeonshouse.moreinterfaces.eventhandler.EntityLoadWorldEventHandler;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerChatEventHandler;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerConnectionEventHandler;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.handlers.message.PlayerEventMessage;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.netty.ConnectionManage;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerEventCommand implements CommandHandler {
    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);
    public static final PlayerEventCommand INSTANCE = new PlayerEventCommand();

    private PlayerEventCommand() {

    }

    @Override
    public void onCommand(ChatSession session, CommandMap map) {
        String event = map.getString(session, "event");
        EventCallback eventCallback = new EventCallback(session, event);

        if (subscribeEvent.containsKey(event)) {
            List<EventCallback> callbacks = subscribeEvent.get(event);
            int index = callbacks.indexOf(eventCallback);
            if (index != -1) {
                if (ConnectionManage.hasUser(callbacks.get(index).session().channel())) {
                    session.sendErrorMsg(MessageCode.ALREADY_SUBSCRIBED, "您已经订阅了该事件！如需重复订阅，请更换会话ID");
                    return;
                }
                callbacks.remove(index);
            }

            callbacks.add(eventCallback);
        } else {
            activateListeners(event);
            subscribeEvent.computeIfAbsent(event, k -> new CopyOnWriteArrayList<>())
                    .add(eventCallback);
        }

        session.sendSucceededMessage();
    }

    /**
     * 取消订阅玩家事件
     */
    public static class UnsubscribePlayerEventCommand implements CommandHandler {
        @Override
        public void onCommand(ChatSession session, CommandMap map) {
            String event = map.getString(session, "event");
            if (!subscribeEvent.containsKey(event)) {
                session.sendErrorMsg(MessageCode.NOT_SUBSCRIBED, "您没有订阅该事件！");
                return;
            }

            String sessionId = map.getStringOrDef("sessionId", session.sessionId());

            boolean removed = subscribeEvent.get(event).removeIf(callback -> {
                ChatSession chatSession = callback.session();

                return (chatSession.channel() == session.channel()) &&
                        Objects.equals(chatSession.sessionId(), sessionId);
            });

            if (removed) {
                session.sendSucceededMessage();
            } else {
                session.sendErrorMsg(MessageCode.NOT_SUBSCRIBED, "您没有订阅该事件或会话id不匹配！");
            }
        }
    }

    public static final Map<String, List<EventCallback>> subscribeEvent = new HashMap<>();

    public record EventCallback(ChatSession session, String event) {
        public void call(MIPlayer player, Map<String, Object> data) {
            if (ConnectionManage.hasUser(session().channel())) {
                session().sendMsg(new PlayerEventMessage(session().sessionId(), event, player, data));
            } else {
                subscribeEvent.get(event).remove(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EventCallback that)) return false;
            return Objects.equals(event, that.event) && Objects.equals(session, that.session);
        }

        @Override
        public int hashCode() {
            return Objects.hash(session, event);
        }
    }

    public static void callEvent(String event, MIPlayer player, Map<String, Object> data) {
        if (subscribeEvent.containsKey(event)) {
            for (EventCallback callback : subscribeEvent.get(event)) {
                try {
                    callback.call(player, data);
                } catch (Exception e) {
                    log.error("调用监听器时发生错误！", e);
                }
            }
        }
    }

    public static void activateListeners(String event) {
        switch (event) {
            case "join", "disconnect" -> {
                if (PlayerConnectionEventHandler.INSTANCE == null) {
                    PlayerConnectionEventHandler.getInstance();
                }
            }
            case "death", "killed" -> {
                if (EntityDeathEventHandler.INSTANCE == null) {
                    EntityDeathEventHandler.getInstance();
                }
            }
            case "chat" -> {
                if (PlayerChatEventHandler.INSTANCE == null) {
                    PlayerChatEventHandler.getInstance();
                }
            }
            case "spawn" -> {
                if (EntityLoadWorldEventHandler.INSTANCE == null) {
                    EntityLoadWorldEventHandler.getInstance();
                }
            }
        }
    }
}
