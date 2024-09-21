package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
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

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(PlayerChatMessage playerChatMessage, ServerPlayer serverPlayer, ChatType.Bound bound, CallbackInfo ci) {

        if (PlayerChatEventHandler.INSTANCE != null) {
            if (!PlayerChatEventHandler.getInstance().allowChatMessage(playerChatMessage.signedContent().decorated(), serverPlayer, bound)) {
                ci.cancel();
            }
        }
    }
}
