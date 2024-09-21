package online.pigeonshouse.moreinterfaces.eventhandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import online.pigeonshouse.moreinterfaces.handlers.MIEntity;
import online.pigeonshouse.moreinterfaces.handlers.MIItem;
import online.pigeonshouse.moreinterfaces.handlers.MIPlayer;
import online.pigeonshouse.moreinterfaces.handlers.commands.PlayerEventCommand;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntityDeathEventHandler {
    public static volatile EntityDeathEventHandler INSTANCE;

    public static EntityDeathEventHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (EntityDeathEventHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EntityDeathEventHandler();
                }
            }
        }
        return INSTANCE;
    }

    public void allowDeath(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        Map<String, Object> map = new HashMap<>();
        map.put("damage_type", damageSource.getMsgId());

        ItemStack itemStack = damageSource.getWeaponItem();

        if (!Objects.isNull(itemStack)) {
            MIItem weaponItem = MIUtil.buildMIItem(itemStack);
            map.put("weapon", weaponItem);
        }

        if (!Objects.isNull(damageSource.getEntity())) {
            Entity sourceEntity = damageSource.getEntity();

            if (sourceEntity instanceof ServerPlayer playerEntity) {
                MIPlayer miPlayer = MIUtil.buildMIPlayer(playerEntity);
                MIEntity dieEntity = MIUtil.buildMIEntity(entity);

                map.put("death", dieEntity);
                PlayerEventCommand.callEvent("killed", miPlayer, map);
            }

            if (entity instanceof ServerPlayer playerEntity) {
                MIEntity attackerEntity = MIUtil.buildMIEntity(sourceEntity);
                MIPlayer diePlayer = MIUtil.buildMIPlayer(playerEntity);

                map.put("attacker", attackerEntity);
                PlayerEventCommand.callEvent("death", diePlayer, map);
            }

            return;
        }

        if (entity instanceof ServerPlayer playerEntity) {
            MIPlayer diePlayer = MIUtil.buildMIPlayer(playerEntity);
            PlayerEventCommand.callEvent("death", diePlayer, map);
        }
    }
}
