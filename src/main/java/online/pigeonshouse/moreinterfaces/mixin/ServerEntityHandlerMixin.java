package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import online.pigeonshouse.moreinterfaces.eventhandler.EntityLoadWorldEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerEntityHandlerMixin {
    @Shadow
    boolean tickingEntities;

    @Inject(method = "add", at = @At("TAIL"))
    private void onTrackingStart(Entity entity, CallbackInfo ci) {
        if (!tickingEntities && EntityLoadWorldEventHandler.INSTANCE != null) {
            ServerLevel level = (ServerLevel) entity.level;
            EntityLoadWorldEventHandler.getInstance().onLoad(entity, level);
        }
    }
}