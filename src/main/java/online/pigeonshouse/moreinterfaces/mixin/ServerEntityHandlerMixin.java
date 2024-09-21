package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import online.pigeonshouse.moreinterfaces.eventhandler.EntityLoadWorldEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public class ServerEntityHandlerMixin {
    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void onTrackingStart(Entity entity, CallbackInfo ci) {
        if (EntityLoadWorldEventHandler.INSTANCE != null) {
            ServerLevel level = (ServerLevel)entity.level();
            EntityLoadWorldEventHandler.getInstance().onLoad(entity, level);
        }
    }
}
