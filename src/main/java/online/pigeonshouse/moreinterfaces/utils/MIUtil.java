package online.pigeonshouse.moreinterfaces.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import online.pigeonshouse.moreinterfaces.handlers.*;


public class MIUtil {
    public static MIItemEntity buildMIItemEntity(ItemEntity itemEntity) {
        MIItemEntity miItemEntity = MIItemEntity.create(
                itemEntity.getDisplayName().getString(),
                itemEntity.getStringUUID(),
                buildMIItemStack(itemEntity.getItem()),
                buildMIPos(itemEntity)
        );
        return miItemEntity;
    }

    public static MIItemStack buildMIItemStack(ItemStack itemStack) {
        MIItemStack miItemStack = MIItemStack.create(buildMIItem(itemStack),
                itemStack.getCount(),
                itemStack.getMaxStackSize());

        return miItemStack;
    }

    public static MIItem buildMIItem(ItemStack stack) {
        return MIItem.of(stack.getDisplayName().getString(),
                Registry.ITEM.getKey(stack.getItem()).toString());
    }

    public static MIPlayer buildMIPlayer(Player player) {
        MIPlayer miPlayer = MIPlayer.create(player.getName().getString(),
                player.getStringUUID(),
                buildMIPos(player));

        miPlayer.setEyeHeight(player.getEyeHeight());
        miPlayer.setYaw(player.getYRot());
        miPlayer.setPitch(player.getXRot());
        return miPlayer;
    }

    public static MIEntity buildMIEntity(Entity entity) {
        MIEntity miEntity = MIEntity.create(entity.getDisplayName().getString(),
                getEntityNamespace(entity),
                entity.getStringUUID(),
                buildMIPos(entity), entity.getEyeHeight());

        miEntity.setEyeHeight(entity.getEyeHeight());
        miEntity.setYaw(entity.getYRot());
        miEntity.setPitch(entity.getXRot());
        return miEntity;
    }

    public static MIPos buildMIPos(Entity entity) {
        return MIPos.create(WorldUtil.getLevelName(entity.level),
                entity.getX(), entity.getY(), entity.getZ());
    }

    public static MIPos buildMIPos(Level level, BlockPos blockPos) {
        return MIPos.create(
                WorldUtil.getLevelName(level),
                blockPos.getX(), blockPos.getY(), blockPos.getZ()
        );
    }

    public static String getEntityNamespace(Entity entity) {
        return entity.getType().getDefaultLootTable().toString();
    }
}
