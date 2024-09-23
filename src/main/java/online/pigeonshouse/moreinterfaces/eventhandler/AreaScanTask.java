package online.pigeonshouse.moreinterfaces.eventhandler;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.handlers.*;
import online.pigeonshouse.moreinterfaces.handlers.commands.BlockEventCommand;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;
import online.pigeonshouse.moreinterfaces.utils.MapUtil;

import java.util.ArrayList;
import java.util.List;

public class AreaScanTask extends ServerLifecycleEventHandler.SimpleTickTask {
    private final MIArea area;
    private final MinecraftServer server;
    private final ServerLevel level;
    private int tick = 0;
    @Getter
    @Setter
    private int period = 20;
    private final ChatSession session;

    public AreaScanTask(ChatSession session, MIArea area, ServerLevel level) {
        super(session);
        this.area = area;
        this.server = MoreInterfaces.MINECRAFT_SERVER.get();
        this.level = level;
        this.session = session;
    }

    @Override
    public Object run() {
        tick++;
        if (tick % period != 0) return null;
        tick = 0;

        MIPos pos1 = area.getPos1();
        MIPos pos2 = area.getPos2();

        AABB aabb = new AABB(pos1.getX(), pos1.getY(), pos1.getZ(),
                pos2.getX(), pos2.getY(), pos2.getZ()).inflate(1, 1, 1);

        return level.getEntitiesOfClass(Entity.class, aabb);
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void runAsync(Object o) {
        if (!(o instanceof List<?>)) {
            return;
        }

        List<Entity> allEntity = (List<Entity>) o;
        List<MIEntity> entities = new ArrayList<>(32);

        for (Entity entity : allEntity) {
            if (entity instanceof LivingEntity) {
                MIEntity miEntity = MIUtil.buildMIEntity(entity);
                entities.add(miEntity);
            }

            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity) entity;
                MIItemEntity miItemEntity = MIUtil.buildMIItemEntity(itemEntity);
                entities.add(miItemEntity);
            }
        }

        if (!BlockEventCommand.callEvent(session, "area_entities_info", MIBlock.EMPTY, MapUtil.of("entities", entities))) {
            ServerLifecycleEventHandler.INSTANCE.removeTickTask(this);
        }
    }
}
