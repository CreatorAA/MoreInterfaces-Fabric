package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.server.level.ServerPlayer;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "doCloseContainer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;removed(Lnet/minecraft/world/entity/player/Player;)V",
            shift = At.Shift.AFTER))
    private void onCloseContainer(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        for (PlayerEventHandler.PlayerContainer.EventHandler handler : PlayerEventHandler.PlayerContainer.eventHandlers) {
            handler.onPlayerCloseContainer(player, player.containerMenu);
        }
    }
}
