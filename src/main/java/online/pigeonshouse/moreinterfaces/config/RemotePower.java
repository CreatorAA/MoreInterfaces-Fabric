package online.pigeonshouse.moreinterfaces.config;

import lombok.Data;

import java.util.HashSet;
import java.util.Objects;

/**
 * 查询权限
 * query.containers 容器查询权限，包括容器：指定位置的容器是否被破坏，是否被打开，容器的容量，容器的使用空间，方块的类型等
 * query.blocks 方块查询权限，包括方块：指定位置的方块是否被破坏，是否被放置，方块的类型等
 * query.inventory 玩家查询权限，包括玩家：指定玩家是否在线，玩家坐标
 * query.world 世界查询权限，包括世界：指定世界的敌对生物上限、数量，世界的难度
 * query.entity 实体查询权限，包括实体：指定矩形区域内所有的实体列表和信息
 *
 * 事件监听回调权限
 * event.block.break 方块破坏事件，当指定矩形区域的位置有方块被破坏的通知
 * event.block.place 方块放置事件，当指定矩形区域的位置有方块被放置的通知
 * event.container.open 容器打开事件，当指定矩形区域的位置有容器被打开的通知
 *
 * 指令权限
 * commands.message 发送消息给玩家的权限
 * commands.message.screen 发送消息给玩家并显示在屏幕上的权限
 */
@Data
public class RemotePower {
    public static final RemotePower DEFAULT = new RemotePower();
    // 游客
    public static final RemotePower GUEST = new RemotePower();

    static {
        DEFAULT.power = 0;
    }

    public RemotePower() {
    }

    public RemotePower(int power, HashSet<String> groups) {
        this.power = power;
        this.groups = groups;
    }

    int power = -1;
    HashSet<String> groups = new HashSet<>();


    public boolean queryGroups(String group) {
        RemoteConfig.Config.INSTANCE.getRootPower()
                .getGroups().add(group);

        return power == 0 || groups.contains(group);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemotePower that)) return false;
        return power == that.power;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(power);
    }
}
