package online.pigeonshouse.moreinterfaces.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import online.pigeonshouse.moreinterfaces.client.ClientCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerMixin {
    @Inject(method = "chat", at = @At("HEAD"), cancellable = true)
    private void onChat(String message, CallbackInfo ci) {
        if (ClientCommand.execute(message)) {
            ci.cancel();
        }
    }
}
