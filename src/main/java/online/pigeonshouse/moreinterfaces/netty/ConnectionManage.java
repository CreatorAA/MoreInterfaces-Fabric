package online.pigeonshouse.moreinterfaces.netty;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 此类考虑后续做拓展， 所以所有业务不使用channel自带的判断在线的方式：channel.isActive()
 */
public class ConnectionManage {
    private static List<Channel> userChannelList = new CopyOnWriteArrayList<>();

    private static final Object lock = new Object();

    /**
     * 添加用户
     */
    public static void addUser(Channel channel) {
        synchronized (lock) {
            userChannelList.add(channel);
        }
    }

    /**
     * 移除用户
     */
    public static void removeUser(Channel channel) {
        synchronized (lock) {
            userChannelList.remove(channel);

            Optional.ofNullable(channel)
                    .ifPresent(channel1 -> {
                        try {
                            channel1.close().sync();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    public static boolean hasUser(Channel channel) {
        return userChannelList.contains(channel);
    }

    public static void clean() {
        synchronized (lock) {
            userChannelList.forEach(ConnectionManage::removeUser);
        }
    }
}
