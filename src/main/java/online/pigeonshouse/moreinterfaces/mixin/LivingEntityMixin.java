package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import online.pigeonshouse.moreinterfaces.eventhandler.EntityDeathEventHandler;
import online.pigeonshouse.moreinterfaces.handlers.MIServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract boolean isDeadOrDying();

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z", ordinal = 1))
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (isDeadOrDying()) {
            LivingEntity entity = (LivingEntity) (Object) this;
            EntityDeathEventHandler.getInstance().allowDeath(entity, source, amount);
        }
    }

    @Inject(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;stopUsingItem()V"
            , shift = At.Shift.AFTER))
    public void completeUsingItem(CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (living instanceof MIServerPlayer) {
            MIServerPlayer player = (MIServerPlayer) living;
            if (player.hasOpenBackpack()) {
                player.getBackpack().update();
            }
        }
    }
}
