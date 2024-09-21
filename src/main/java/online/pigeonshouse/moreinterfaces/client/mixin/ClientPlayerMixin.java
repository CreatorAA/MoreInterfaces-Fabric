package online.pigeonshouse.moreinterfaces.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import online.pigeonshouse.moreinterfaces.client.ClientCommand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerMixin {
    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void sendCommand(String string, @Nullable Component component, CallbackInfo ci) {
        if (ClientCommand.execute(string)) {
            ci.cancel();
        }
    }
}
