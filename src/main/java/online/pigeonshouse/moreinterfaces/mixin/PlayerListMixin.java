package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerChatEventHandler;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerConnectionEventHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow
    public abstract @Nullable ServerPlayer getPlayer(UUID uUID);

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlaceNewPlayer(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        if (PlayerConnectionEventHandler.INSTANCE != null) {
            PlayerConnectionEventHandler.getInstance().onPlayReady(serverPlayer, serverPlayer.getServer());
        }
    }

    @Inject(method = "broadcastMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(Component component, ChatType chatType, UUID uuid, CallbackInfo ci) {
        if (PlayerChatEventHandler.INSTANCE != null) {
            ServerPlayer player = getPlayer(uuid);

            if (player == null) return;
            if (!PlayerChatEventHandler.getInstance().allowChatMessage(component, player, chatType)) {
                ci.cancel();
            }
        }
    }
}
