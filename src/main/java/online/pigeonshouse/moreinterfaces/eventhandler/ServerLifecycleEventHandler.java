package online.pigeonshouse.moreinterfaces.eventhandler;

import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerLifecycleEventHandler {
    public static final ServerLifecycleEventHandler INSTANCE = new ServerLifecycleEventHandler();

    private final List<TickTask> tickTasks = new CopyOnWriteArrayList<>();

    public synchronized void removeAll() {
        tickTasks.clear();
    }

    public synchronized void removeTickTask(TickTask task) {
        tickTasks.remove(task);
    }

    public synchronized void removeTickTask(String taskID) {
        tickTasks.removeIf(task -> task.getTaskID().equals(taskID));
    }

    public synchronized boolean addTickTask(TickTask task) {
        if (tickTasks.contains(task)) {
            return false;
        }

        tickTasks.add(task);
        return true;
    }

    public TickTask getTask(String taskID) {
        return tickTasks.stream().filter(task -> task.getTaskID().equals(taskID)).
                findFirst().orElse(null);
    }

    public void onServerStarted(MinecraftServer server) {
        MoreInterfaces.MORE_INTERFACES.get().onServerStarted(server);
    }

    public void onServerStopping() {
        MoreInterfaces.MORE_INTERFACES.get().onServerStopping();
    }

    public void onServerTickEnd() {
        for (TickTask task : tickTasks) {
            Object run = task.run();
            if (run != null && task.isAsync()) {
                MoreInterfaces.MORE_INTERFACES.get().runThread(() -> task.runAsync(run));
            }
        }
    }

    public interface TickTask {
        String getTaskID();
        ChatSession getSession();
        Object run();

        boolean isAsync();
        void runAsync(Object o);
    }

    @Getter
    public static abstract class SimpleTickTask implements TickTask {
        private final String taskID;
        private final ChatSession session;

        public SimpleTickTask(ChatSession session) {
            this.session = session;
            this.taskID = session.sessionId();
        }

        @Override
        public void runAsync(Object o) {

        }

        @Override
        public boolean isAsync() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleTickTask)) return false;
            SimpleTickTask that = (SimpleTickTask) o;
            return Objects.equals(getTaskID(), that.getTaskID());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(taskID);
        }
    }

}
