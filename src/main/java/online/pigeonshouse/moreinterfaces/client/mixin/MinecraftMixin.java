package online.pigeonshouse.moreinterfaces.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import online.pigeonshouse.moreinterfaces.client.commands.SaveAllItemsCommand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Nullable public Screen screen;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void setScreen(Screen sc, CallbackInfo info) {
        if (screen instanceof SaveAllItemsCommand.SaveAllItemProgressBarScreen &&
            sc == null && !Thread.currentThread().getStackTrace()[3]
                .getClassName()
                .startsWith("online.pigeonshouse.moreinterfaces.client.commands.SaveAllItemsCommand")) {
            info.cancel();
        }
    }
}
