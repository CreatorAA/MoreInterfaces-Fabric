package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import online.pigeonshouse.moreinterfaces.gui.MIServerPlayerBackPack;
import online.pigeonshouse.moreinterfaces.handlers.MIServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "interactAt", at = @At("HEAD"))
    private void interactAt(Player player, Vec3 vec3, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Entity entity = (Entity) (Object) this;
        if (player instanceof ServerPlayer && entity instanceof MIServerPlayer
                && player.getItemInHand(hand).isEmpty()) {

            MIServerPlayer miServerPlayer = (MIServerPlayer) entity;
            if (!miServerPlayer.hasOpenBackpack()) {
                MIServerPlayerBackPack.openMIPlayerMenu(miServerPlayer, (ServerPlayer) player);
            }
        }
    }
}
