package online.pigeonshouse.moreinterfaces.eventhandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerEventHandler {
    public static class MIPlayerItem {
        public static final List<EventHandler> eventHandlers = new CopyOnWriteArrayList<>();

        public interface EventHandler {
            void onPlayerItem(ServerPlayer player);
        }
    }

    public static class PlayerContainer {
        public static final List<EventHandler> eventHandlers = new CopyOnWriteArrayList<>();

        public interface EventHandler {
            void onPlayerCloseContainer(ServerPlayer player, AbstractContainerMenu menu);
        }
    }
}
