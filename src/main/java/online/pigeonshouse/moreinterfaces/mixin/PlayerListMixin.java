package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerChatEventHandler;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerConnectionEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlaceNewPlayer(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        if (PlayerConnectionEventHandler.INSTANCE != null) {
            PlayerConnectionEventHandler.getInstance().onPlayReady(serverPlayer, serverPlayer.getServer());
        }
    }

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/server/network/FilteredText;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(FilteredText<PlayerChatMessage> filteredText, ServerPlayer serverPlayer, ResourceKey<ChatType> resourceKey, CallbackInfo ci) {

        if (PlayerChatEventHandler.INSTANCE != null) {
            if (!PlayerChatEventHandler.getInstance().allowChatMessage(filteredText.filtered().serverContent(), serverPlayer, resourceKey)) {
                ci.cancel();
            }
        }
    }
}
